package za.org.grassroot2.services

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.*
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.exception.*
import za.org.grassroot2.model.language.NluResponse
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.model.network.EntityForUpload
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.*
import za.org.grassroot2.services.rest.ApiError
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
constructor(private val grassrootUserApi: GrassrootUserApi,
            private val databaseService: DatabaseService) : NetworkService {

    override fun uploadEntity(entityForUpload: EntityForUpload, forceEvenIfPriorUploaded: Boolean): Observable<UploadResult> {
        return routeUpload(entityForUpload, forceEvenIfPriorUploaded)
    }

    override fun <E : EntityForDownload> downloadAllChangedOrNewEntities(entityType: GrassrootEntityType, forceFullRefresh: Boolean): Observable<List<E>> {
        when (entityType) {
            GrassrootEntityType.GROUP -> return downloadAllChangedOrNewGroups()
                    .flatMap { groups -> Observable.just(groups as List<E>) }
            else -> throw IllegalArgumentException("Error! Trying to download an unimplemented type")
        }
    }

    override fun downloadAllChangedOrNewGroups(): Observable<List<Group>> {
        return grassrootUserApi
                .fetchUserGroups(databaseService.loadExistingObjectsWithLastChangeTime(Group::class.java))
                .doOnError({ Timber.e(it) })
                .filter { listRestResponse ->
                    Timber.e("filtering if group list empty, what does map look like? %s", listRestResponse)
                    !listRestResponse.isEmpty()
                }
                .concatMap { groups ->
                    Timber.e("getting group info for remainder")
                    val changedUids = groups.indices.map { groups[it].uid }
                    grassrootUserApi.fetchGroupsInfo(changedUids)
                }
                .doOnError({ Timber.e(it) })
                .flatMap { listRestResponse ->
                    Timber.d("alright, here are the full groups back: %s", listRestResponse)
                    Observable.just(listRestResponse)
                }
    }

    override fun getTimestampForText(date: String): Observable<Long> {
        return grassrootUserApi.getTimestampForTextDate(date).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun seekIntentInText(text: String): Flowable<NluResponse> {
        return grassrootUserApi.seekIntentInText(text).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun downloadTaskMinimumInfo(): Observable<List<Task>> {
        return grassrootUserApi
                .fetchUserTasksMinimumInfo(databaseService.getAllTasksLastChangedTimestamp())
                .doOnError { Timber.e(it) }
    }

    override fun fetchPendingResponses(): Observable<PendingResponseDTO> {
        return grassrootUserApi
                .fetchPendingResponses()
                .doOnError { Timber.e(it) }
    }

    override fun inviteContactsToGroup(groupId: String, contacts: List<MemberRequest>): Observable<Response<Void>> {
        return Observable.create { e: ObservableEmitter<Response<Void>> ->
            object : UploadResource<List<MemberRequest>>(contacts, e) {
                override fun uploadRemote(localObject: List<MemberRequest>): Observable<Response<Void>> =
                        grassrootUserApi.addMembersToGroup(groupId, localObject)

                override fun uploadFailed(localObject: List<MemberRequest>) {
                    databaseService.storeMembersInvites(localObject)
                }
            }
        }
    }

    override fun hideGroup(group: Group): Observable<Boolean> {
        return grassrootUserApi.hideGroup(group.uid)
                .flatMap { response -> Observable.just(response.isSuccessful)}
                .onErrorResumeNext { t: Throwable ->
                    Timber.e(t, "Error hiding group in call to network!")
                    Observable.just(false)
                }
    }

    override fun leaveGroup(group: Group): Observable<Boolean> {
        return grassrootUserApi.leaveGroup(group.uid)
                .flatMap { response ->
                    if (response.status == "SUCCESS") {
                        databaseService.removeGroup(group.uid)
                    }
                    Observable.just(response.status == "SUCCESS")
                }
                .onErrorResumeNext { throwable: Throwable ->
                    Timber.e(throwable, "Error leaving group!")
                    Observable.just(false)
                };
    }

    override fun downloadMemberFile(group: Group): Observable<ByteArray> {
        return grassrootUserApi.fetchGroupMemberSheet(group.uid)
                .subscribeOn(io())
                .filter({ r -> r.isSuccessful })
                .flatMap { r -> Observable.just(r.body()?.bytes()) } // todo : maybe still throw error if doesn't work (use Resource?)
    }

    override fun downloadCompleteGroupInfo(groupId: String): Observable<Group> {
        return grassrootUserApi.fetchFullGroupInfo(groupId)
                .subscribeOn(io())
                .flatMap { group ->
                    Timber.d("got this group back: $group, with members: ${group.memberships?.size}")
                    databaseService.storeGroupWithMembers(group).toObservable()
                }
    }

    override fun getTasksForGroup(groupId: String): Observable<List<Task>> {
        return grassrootUserApi.fetchGroupTasksMinimumInfo(groupId, databaseService.getTasksLastChangedTimestamp(groupId)).flatMap { listRestResponse ->
            if (true) {
                val uids = HashMap<String, String>()
                for (t in listRestResponse) {
                    uids.put(t.uid, t.type.name)
                }
                grassrootUserApi.fetchTasksByUid(uids)
            } else {
                Observable.just(ArrayList<Task>())
            }
        }
    }

    override fun fetchTodoResponses(taskUid: String): Observable<Map<String, String>> {
        val responses = grassrootUserApi
                .fetchTodoResponses(taskUid)
                .doOnError({ Timber.e(it)})
        return responses
    }

    override fun downloadTodoResponses(taskUid: String): Observable<ByteArray>? {
        return grassrootUserApi
                .downloadTodoResponses(taskUid)
                .doOnError({ Timber.e(it)})
    }

    override fun getTasksByUids(uids: Map<String, String>): Observable<List<Task>> {
        return grassrootUserApi
                .fetchTasksByUid(uids)
                .doOnError({ Timber.e(it) })
    }

    override fun createGroup(group: Group): Observable<Resource<Group>> {
        return Observable.create { e ->
            object : ResourceToStore<Group, Group>(group, e) {
                override fun uploadRemote(localObject: Group): Observable<Response<Group>> {
                    Timber.d("Sending new group to server.")
                    return grassrootUserApi.createGroup(group.name, group.description, group.userRole, group.reminderMinutes, group.isHidden, group.isDefaultAddToAccount, group.isPinned )
                }
                override fun uploadFailed(localObject: Group) {
                    Timber.d("createGroup uploadFailed")
                    val g = localObject as Group
                    g.isSynced = false
                    //databaseService.storeGroupWithMembers(group)
                }

                override fun saveResult(data: Group?) {
                    databaseService.storeGroupWithMembers(group).toObservable()
                    Timber.d("Saving result from server")
                }
            }
        }
    }

    override fun createTask(t: Task): Observable<Resource<Task>> {
        Timber.e("Task type is: %s", t.type.toString())
        return Observable.create { e ->
            object : ResourceToStore<Task, Task>(t, e) {
                override fun uploadRemote(localObject: Task): Observable<Response<Task>> {
                    if (t.type.toString() == "MEETING") {
                        val m = localObject as Meeting
                        Timber.d("Here's what I'm sending: %s", m.toString())
                        return grassrootUserApi.createMeeting("GROUP", m.parentUid, m.name,m.locationDescription, m.deadlineMillis, m.description, true, m.latitude, m.longitude,  m.assignedMemberUids, m.mediaFileUid)
                    }
                    else if (t.type.toString() == "VOTE") {
                        val v = localObject as Vote
                        Timber.d("Here's what I'm sending: %s", v.toString())
                        return grassrootUserApi.createVote("GROUP", v.parentUid, v.name, v.voteOptions, v.description, v.deadlineMillis, v.mediaFileUid, v.assignedMemberUids)
                    }
                    else if (t.type.toString() == "TODO") {
                        val todo = localObject as Todo
                        Timber.d("Here's what I'm sending: %s", todo.toString())
                        // as there are 4 different To-do creation api paths we must distinguish between todos
                        if (todo.todoType == "ACTION_REQUIRED") {
                            return grassrootUserApi.createActionTodo("GROUP", todo.parentUid, todo.name, todo.deadlineMillis, todo.isRecurring, todo.recurringPeriodMillis, todo.assignedMemberUids, todo.mediaFileUids)
                        }
                        else if (todo.todoType == "INFORMATION_REQUIRED") {
                            return grassrootUserApi.createInformationTodo("GROUP", todo.parentUid, todo.name, todo.responseTag, todo.deadlineMillis, todo.assignedMemberUids, todo.mediaFileUids)
                        }
                        else if (todo.todoType == "VALIDATION_REQUIRED") {
                            return grassrootUserApi.createConfirmationTodo("GROUP", todo.parentUid, todo.name, todo.deadlineMillis, todo.isRequireImages, todo.assignedMemberUids, todo.confirmingMemberUids, todo.isRecurring, todo.recurringPeriodMillis, todo.mediaFileUids)
                        }
                        else if (todo.todoType == "VOLUNTEERS_NEEDED") {
                            return grassrootUserApi.createVolunteerTodo("GROUP", todo.parentUid, todo.name, todo.deadlineMillis, todo.assignedMemberUids, todo.mediaFileUids)
                        }
                    }
                    throw IllegalArgumentException("Error, no task type.")
                }

                override fun uploadFailed(localObject: Task) {
                    if (localObject.parentEntityType == GrassrootEntityType.MEETING) {
                        val m = localObject as Meeting
                        m.isSynced = false
                    }
                    else if (localObject.parentEntityType == GrassrootEntityType.VOTE) {
                        val v = localObject as Vote
                        v.isSynced = false
                    }
                    else if (localObject.parentEntityType == GrassrootEntityType.TODO) {
                        val td = localObject as Todo
                        td.isSynced = false
                    }
                    databaseService.storeTasks(listOf<Task>(localObject))
                }

                override fun saveResult(data: Task?) {
                    data?.let { databaseService.storeTasks(listOf(data)) }
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
                alert.headline,
                alert.description,
                alert.alertType,
                alert.groupUid,
                alert.taskUid,
                false,
                0.0,
                0.0,
                alert.mediaFileKeys)
            .flatMap(successHandler(alert))
            .onErrorResumeNext(resumeHandler(alert))
            .concatMap { uploadResult ->
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
                Observable.just(UploadResult(alert.type, alert.uid, restResponseResponse.body()!!.data))
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
        return grassrootUserApi.getAlertsAround(longitude, latitude, radius)
    }

    override fun getAllAround(longitude: Double, latitude: Double, radius: Int): Flowable<Resource<List<AroundEntity>>> {
        return Flowable.create({ e ->
            object : NetworkResource<List<AroundEntity>, List<AroundEntity>>(e) {
                override fun local(): Maybe<List<AroundEntity>> {
                    return databaseService.load(AroundEntity::class.java)
                }

                override fun remote(): Observable<List<AroundEntity>> =
                        grassrootUserApi.getAllAround(longitude, latitude, radius, "BOTH")

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

    override fun respondToMeeting(meetingUid: String, response: String): Observable<Response<Void>> =
            grassrootUserApi.respondToMeeting(meetingUid, response)

    override fun respondToVote(voteUid: String, response: String): Observable<Vote> {
        return grassrootUserApi.respondToVote(voteUid, response).flatMap { serverResponse ->
            if (serverResponse.isSuccessful) {
                Observable.just(serverResponse.body())
            } else {
                when (ApiError(serverResponse.errorBody()).errorCode) {
                    "USER_NOT_PART_OF_VOTE" -> throw UserNotPartOfTaskException()
                    "VOTE_ALREADY_CLOSED" -> throw VoteClosedException()
                    else -> throw GenericApiException(serverResponse.errorBody())
                }
            }
        }
    }

    override fun respondToTodo(todoUid: String, response: String): Observable<Todo> {
        return grassrootUserApi.respondToTodo(todoUid, response).flatMap { serverResponse ->
            if (serverResponse.isSuccessful) {
                Observable.just(serverResponse.body())
            } else {
                when (ApiError(serverResponse.errorBody()).errorCode) {
                    "USER_NOT_PART_OF_TODO" -> throw UserNotPartOfTaskException()
                    "TODO_ALREADY_CLOSED" -> throw IllegalArgumentException()
                    else -> throw GenericApiException(serverResponse.errorBody())
                }
            }
        }
    }

    override fun uploadMeetingPost(meetingUid: String, description: String, mediaFile: MediaFile?): Observable<Response<Void>> {
        return grassrootUserApi.uploadPost(
                "MEETING",
                meetingUid,
                description,
                if (mediaFile != null) getFileFromPath(mediaFile, "image") else null
        )
    }

    override fun uploadSpeech(sampleRate: Int, parseForIntent: Boolean, filePath: String): Observable<Response<Void>> =
            grassrootUserApi.uploadSpeech(sampleRate, parseForIntent, getFileFromPath(filePath, "file"))

    private fun uploadMediaFile(mediaFile: MediaFile): Observable<UploadResult> {
        mediaFile.initUploading()
        databaseService.storeObject(MediaFile::class.java, mediaFile)
        return grassrootUserApi.sendMediaFile(
                mediaFile.uid,
                mediaFile.mediaFunction,
                mediaFile.mimeType,
                getFileFromPath(mediaFile, "file"))
            .flatMap(successHandler(mediaFile)).onErrorResumeNext(resumeHandler(mediaFile)).concatMap { uploadResult ->
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

    override fun getMeetingPosts(taskUid: String): Flowable<Resource<List<Post>>> {
        return Flowable.create({ e ->
            object : NetworkResource<List<Post>, List<Post>>(e) {

                override fun local(): Maybe<List<Post>> = databaseService.getMeetings(taskUid)

                override fun remote(): Observable<List<Post>> =
                        grassrootUserApi.getPostsForTask("MEETING", taskUid)

                override fun saveResult(data: List<Post>) {
                    val meeting = databaseService.loadObjectByUid(Meeting::class.javaObjectType, taskUid)
                    databaseService.storePosts(meeting!!, data)
                }

                override fun shouldFetch(): Boolean = true

            }
        }, BackpressureStrategy.BUFFER)

    }

    override fun uploadGroupProfilePhoto(groupUid: String, multipartBody: MultipartBody.Part?):Observable<Response<MediaUploadResult>> {
        return grassrootUserApi.uploadGroupProfilePhoto(groupUid,multipartBody)
                .doOnError { Timber.e(it) }
    }


    private fun getFileFromPath(mediaFile: MediaFile, paramName: String): MultipartBody.Part? {
        return try {
            Timber.i("getting image from path : %s", mediaFile.absolutePath)
            val file = File(mediaFile.absolutePath)
            Timber.d("file size : %s", file.length() / 1024)
            val requestFile = RequestBody.create(MediaType.parse(mediaFile.mimeType), file)
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun getFileFromPath(filePath: String, paramName: String): MultipartBody.Part? {
        return try {
            Timber.i("getting image from path : %s", filePath)
            val file = File(filePath)
            Timber.d("file size : %s", file.length() / 1024)
            val requestFile = RequestBody.create(MediaType.parse(""), file)
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}

