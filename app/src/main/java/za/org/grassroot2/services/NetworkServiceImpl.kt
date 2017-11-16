package za.org.grassroot2.services

import android.text.TextUtils
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.UploadResult
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.exception.EntityAlreadyUploadingException
import za.org.grassroot2.model.exception.ServerErrorException
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.model.network.EntityForUpload
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.services.rest.RestResponse
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Created by luke on 2017/08/16.
 */

class NetworkServiceImpl @Inject
constructor(private val userDetailsService: UserDetailsService,
            private val grassrootUserApi: GrassrootUserApi,
            private val databaseService: DatabaseService) : NetworkService {

    private var currentUserUid: String? = null // given frequency of calling/using, best to stash

    init {
        currentUserUid = userDetailsService.currentUserUid
    }

    override fun uploadEntity(entityForUpload: EntityForUpload, forceEvenIfPriorUploaded: Boolean): Observable<UploadResult> {
        currentUserUid = userDetailsService.currentUserUid
        return routeUpload(entityForUpload, forceEvenIfPriorUploaded)
    }

    // as with below (upload method), know there must be a more RX 'pure' pattern to do this than passing
    // along the emitter, but I'm struggling to work out what, and defaulting to get work -> get clean
    override fun <E : EntityForDownload> downloadAllChangedOrNewEntities(entityType: GrassrootEntityType, forceFullRefresh: Boolean): Observable<List<E>> {
        Timber.e("user UID = ? " + currentUserUid!!)
        when (entityType) {
            GrassrootEntityType.GROUP -> return downloadAllChangedOrNewGroups()
                    .flatMap { groups -> Observable.just(groups as List<E>) }
            else -> throw IllegalArgumentException("Error! Trying to download an unimplemented type")
        }
    }

    override fun downloadAllChangedOrNewGroups(): Observable<List<Group>> {
        return grassrootUserApi
                .fetchUserGroups(currentUserUid, databaseService.loadExistingObjectsWithLastChangeTime(Group::class.java))
                .doOnError({ Timber.e(it) })
                .filter { listRestResponse ->
                    Timber.e("filtering if group list empty, what does map look like? " + listRestResponse)
                    !listRestResponse.isEmpty()
                }
                .concatMap { groups ->
                    Timber.e("getting group info for remainder")
                    val changedUids = ArrayList<String>()
                    for (i in groups.indices) {
                        changedUids.add(groups[i].uid)
                    }
                    grassrootUserApi.fetchGroupsInfo(currentUserUid, changedUids)
                }
                .doOnError({ Timber.e(it) })
                .flatMap { listRestResponse ->
                    Timber.d("alright, here are the full groups back: " + listRestResponse)
                    Observable.just(listRestResponse)
                }
    }

    override fun getTimestampForText(date: String): Observable<Long> {
        return grassrootUserApi.getTimestampForTextDate(date).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun downloadTaskMinimumInfo(): Observable<List<Task>> {
        return grassrootUserApi
                .fetchUserTasksMinimumInfo(currentUserUid, databaseService.getAllTasksLastChangedTimestamp())
                .doOnError({ Timber.e(it) })
    }

    override fun inviteContactsToGroup(groupId: String, contacts: List<MemberRequest>): Observable<Response<Void>> {
        return Observable.create { e: ObservableEmitter<Response<Void>> ->
            object : UploadResource<List<MemberRequest>>(contacts, e) {
                override fun uploadRemote(localObject: List<MemberRequest>): Observable<Response<Void>> {
                    return grassrootUserApi.addMembersToGroup(currentUserUid, groupId, localObject)
                }

                override fun uploadFailed(localObject: List<MemberRequest>) {
                    databaseService.storeMembersInvites(localObject)
                }
            }
        }
    }

    override fun getTasksForGroup(groupId: String): Observable<List<Task>> {
        return grassrootUserApi.fetchGroupTasksMinimumInfo(currentUserUid, groupId, databaseService.getTasksLastChangedTimestamp(groupId)).flatMap { listRestResponse ->
            if (true) {
                val uids = HashMap<String, String>()
                for (t in listRestResponse) {
                    uids.put(t.uid, t.type.name)
                }
                grassrootUserApi.fetchTasksByUid(currentUserUid, uids)
            } else {
                Observable.just(ArrayList<Task>())
            }
        }
    }

    override fun getTasksByUids(uids: Map<String, String>): Observable<List<Task>> {
        return grassrootUserApi
                .fetchTasksByUid(currentUserUid, uids)
                .doOnError({ Timber.e(it) })
    }

    override fun createTask(t: Task): Observable<Resource<Task>> {
        return Observable.create { e ->
            object : ResourceToStore<Task, Task>(t, e) {
                override fun uploadRemote(localObject: Task): Observable<Response<Task>> {
                    val m = localObject as Meeting
                    return grassrootUserApi.createTask("GROUP", currentUserUid, t.parentUid, m.name, m.locationDescription, t.deadlineMillis)
                }

                override fun uploadFailed(localObject: Task) {
                    val m = localObject as Meeting
                    m.isSynced = false
                    databaseService.storeTasks(listOf<Task>(localObject))
                }

                override fun saveResult(data: Task) {
                    databaseService.storeTasks(listOf(data))
                }
            }
        }
    }

    private fun routeUpload(entity: EntityForUpload, forceUpload: Boolean): Observable<UploadResult> {
        if (entity.isUploading) {
            return Observable.just(UploadResult(entity.type, EntityAlreadyUploadingException()))
        } else if (!forceUpload && entity.isUploaded) {
            return Observable.just(UploadResult(entity.type, entity))
        } else {
            val hasPriorEntitiesToUpload = entity.priorEntitiesToUpload() != null && !entity.priorEntitiesToUpload()!!.isEmpty()
            val mainEntityUpload: Observable<UploadResult> = when (entity.type) {
                GrassrootEntityType.MEDIA_FILE -> uploadMediaFile(entity as MediaFile)
                GrassrootEntityType.LIVE_WIRE_ALERT -> uploadLiveWireAlert(entity as LiveWireAlert)
                else -> return Observable.just(UploadResult(entity.type, IllegalArgumentException("Unsupported type for uploading to retrieve UID")))
            }
            return if (!hasPriorEntitiesToUpload)
                mainEntityUpload
            else
                clearPriorUploads(entity.priorEntitiesToUpload()!!)
                        .concatMap { mainEntityUpload }
        }
    }

    // todo : be careful of exactly how merging is done in here, merge is probably not the right operator
    private fun clearPriorUploads(priorQueue: List<EntityForUpload>): Observable<UploadResult> {
        val uploadResults = priorQueue.indices.map { routeUpload(priorQueue[it], false) }
        return Observable.merge(uploadResults)
    }

    private fun uploadLiveWireAlert(alert: LiveWireAlert): Observable<UploadResult> {
        return grassrootUserApi.createLiveWireAlert(
                currentUserUid,
                alert.headline,
                if (TextUtils.isEmpty(alert.description)) "" else alert.description, // very temp hack to avoid a redeploy of main platform just to make required (remove in future)
                alert.alertType,
                alert.groupUid,
                alert.taskUid,
                false,
                0.0,
                0.0,
                alert.mediaFileKeys).flatMap(successHandler(alert)).onErrorResumeNext(resumeHandler(alert)).concatMap { uploadResult ->
            if (uploadResult.serverUid != null) {
                alert.serverUid = uploadResult.serverUid
                alert.setUnderReview(true)
                databaseService.storeObject(LiveWireAlert::class.java, alert)
            }
            Observable.just(uploadResult)
        }
    }

    private fun successHandler(alert: EntityForUpload): Function<Response<RestResponse<String>>, ObservableSource<out UploadResult>> {
        return Function { restResponseResponse ->
            if (restResponseResponse.isSuccessful()) {
                Observable.just(UploadResult(alert.type, alert.uid, restResponseResponse.body()!!.getData()))
            } else {
                Observable.just(UploadResult(alert.type, ServerErrorException(restResponseResponse.code())))
            }
        }
    }

    private fun resumeHandler(entity: EntityForUpload): Function<Throwable, ObservableSource<out UploadResult>> {
        return Function { throwable ->
            if (throwable is IOException) {
                Observable.just(UploadResult(entity.type, Throwable()))
            } else {
                Observable.just(UploadResult(entity.type, IllegalArgumentException()))
            }
        }
    }

    override fun getAlertsAround(longitude: Double, latitude: Double, radius: Int): Observable<List<LiveWireAlert>> {
        return grassrootUserApi.getAlertsAround(currentUserUid, longitude, latitude, radius)
    }

    override fun getAllAround(longitude: Double, latitude: Double, radius: Int): Flowable<Resource<List<AroundEntity>>> {
        return Flowable.create({ e ->
            object : NetworkResource<List<AroundEntity>, List<AroundEntity>>(e) {
                override fun local(): Maybe<List<AroundEntity>> {
                    return databaseService.load(AroundEntity::class.java)
                }

                override fun remote(): Observable<List<AroundEntity>> =
                        grassrootUserApi.getAllAround(currentUserUid, longitude, latitude, radius, "BOTH")

                override fun saveResult(data: List<AroundEntity>) {
                    databaseService.deleteAll(AroundEntity::class.java)
                    data.forEach { entity -> databaseService.storeObject(AroundEntity::class.java, entity) }
                }

                override fun shouldFetch(): Boolean {
                    return true
                }
            }
        }, BackpressureStrategy.BUFFER)

    }

    override fun respondToMeeting(meetingUid: String, response: String): Observable<Response<Void>> = grassrootUserApi.respondToMeeting(currentUserUid, meetingUid, response)

    override fun uploadMeetingPost(meetingUid: String, description: String, mediaFile: MediaFile?): Observable<Response<Void>> {
        return grassrootUserApi.uploadPost(
                currentUserUid,
                "MEETING",
                meetingUid,
                description,
                if (mediaFile != null) getImageFromPath(mediaFile, "image") else null
        )
    }

    private fun uploadMediaFile(mediaFile: MediaFile): Observable<UploadResult> {
        mediaFile.initUploading()
        databaseService.storeObject(MediaFile::class.java, mediaFile)
        return grassrootUserApi.sendMediaFile(
                currentUserUid,
                mediaFile.uid,
                mediaFile.mediaFunction,
                mediaFile.mimeType,
                getImageFromPath(mediaFile, "file")).flatMap(successHandler(mediaFile)).onErrorResumeNext(resumeHandler(mediaFile)).concatMap { uploadResult ->
            if (uploadResult.uploadException == null) {
                mediaFile.haltUploading(true)
                mediaFile.isSentUpstream = true
                mediaFile.serverUid = uploadResult.serverUid
            } else {
                mediaFile.haltUploading(false)
            }
            databaseService.storeObject(MediaFile::class.java, mediaFile)
            Observable.just(uploadResult)
        }
    }

    private fun getImageFromPath(mediaFile: MediaFile, paramName: String): MultipartBody.Part? {
        return try {
            Timber.i("getting image from path : " + mediaFile.absolutePath)
            val file = File(mediaFile.absolutePath)
            Timber.d("file size : " + file.length() / 1024)
            val requestFile = RequestBody.create(MediaType.parse(mediaFile.mimeType), file)
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}

