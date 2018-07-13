package za.org.grassroot2.presenter.activity

import android.net.Uri
import android.text.TextUtils
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.*
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.util.MediaRecorderWrapper
import za.org.grassroot2.view.GrassrootView
import za.org.grassroot2.view.activity.GroupDetailsActivity
import java.io.File
import java.util.*
import java.util.function.Predicate
import javax.inject.Inject


class CreateActionPresenter @Inject
constructor(private val networkService: NetworkService, private val dbService: DatabaseService, private val mediaService: MediaService) : BasePresenter<CreateActionPresenter.CreateActionView>() {

    var task: Task? = null
        private set

    var group: Group? = null
        private set

    private var currentMediaFileUid: String? = null

    var alert: LiveWireAlert? = null
        private set

    val alertAndGroupName: Maybe<Pair<String, LiveWireAlert>>
        get() = dbService.load(Group::class.java, alert!!.groupUid).map { group -> Pair(group.name, alert!!) }

    fun handleAudio(data: Uri?) {
        data?.let {
            disposableOnDetach(mediaService.storeMediaFile(File(data.path).absolutePath).subscribe({
                currentMediaFileUid = it
                alert!!.mediaFile = dbService.loadObjectByUid(MediaFile::class.java, it)
            }, { handleMediaError(it) }))
        }
    }

    enum class ActionType {
        Meeting, LivewireAlert, Vote, Todo, Group
    }

    fun initTask(type: ActionType) {
        when (type) {
            CreateActionPresenter.ActionType.Meeting -> {
                task = Meeting()
                task!!.uid = UUID.randomUUID().toString()
            }
            CreateActionPresenter.ActionType.Todo -> {
                task = Todo()
                task!!.uid = UUID.randomUUID().toString()
            }
            CreateActionPresenter.ActionType.Vote -> {
                task = Vote()
                task!!.uid = UUID.randomUUID().toString()
            }
            CreateActionPresenter.ActionType.LivewireAlert -> alert = LiveWireAlert()
        }
    }

    fun initGroup(): String {
        group = Group()
        group!!.uid = UUID.randomUUID().toString()
        group!!.userRole = "CLOSED_GROUP"
        return group!!.uid
    }

    fun createTask(Type: GrassrootEntityType) {
        view.showProgressBar()
        disposableOnDetach(networkService.createTask(task!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ task ->
            Timber.e("subscriber completed, task created")
            view.closeProgressBar()
            view.uploadSuccessfull(Type)
        }) { throwable ->
            view.closeProgressBar()
            view.uploadSuccessfull(Type)
        })
    }

    fun createGroup(groupUid: String) {
        view.showProgressBar()
        disposableOnDetach(networkService.createGroup(group!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ group ->
            view.closeProgressBar()
            GroupDetailsActivity.start(view.activity, groupUid)
        }) { throwable ->
            view.closeProgressBar()
            view.uploadSuccessfull(GrassrootEntityType.GROUP)
        })
    }

    fun createAlert() {
        if (alert!!.areMinimumFieldsComplete()) {
            view.showProgressBar()
            alert!!.setComplete(true)
            if (currentMediaFileUid != null) {
                alert!!.mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid!!)
            }
            disposableOnDetach(dbService.store<LiveWireAlert>(LiveWireAlert::class.java, alert!!).flatMapObservable { liveWireAlert1 -> networkService.uploadEntity(liveWireAlert1, false) }.flatMap { uploadResult ->
                if (!TextUtils.isEmpty(uploadResult.getServerUid())) {
                    alert!!.serverUid = uploadResult.getServerUid()
                } else {
                    alert!!.isSynced = false
                }
                dbService.storeObject(LiveWireAlert::class.java, alert!!)
                Observable.just(uploadResult)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->
                view.closeProgressBar()
                view.uploadSuccessfull(GrassrootEntityType.LIVE_WIRE_ALERT)
            }) { _ ->
                view.closeProgressBar()
                view.uploadSuccessfull(GrassrootEntityType.LIVE_WIRE_ALERT)
            })
        }
    }

    fun verifyGroupPermissions(groupUid: String?) {
        if (groupUid != null) {
            dbService.load(Group::class.java, groupUid).subscribe({ group -> view.proceedWithRender(group) }, { it.printStackTrace() })
        } else {
            view.proceedWithRender(null)
        }
    }

    fun canCreateMeeting():Boolean{
        val groups:List<Group> = dbService.loadGroupsSorted()
        var canCreate = false
        if(!groups.isEmpty()){
            canCreate = groups.any { group -> group.permissions.contains(GroupPermission.CREATE_GROUP_MEETING) }
        }
        return canCreate
    }

    fun canCreateTodo():Boolean{
        val groups:List<Group> = dbService.loadGroupsSorted()
        var canCreate = false
        if(!groups.isEmpty()){
            canCreate = groups.any { group -> group.permissions.contains(GroupPermission.CREATE_GROUP_ENTRY) }
        }
        return canCreate
    }

    fun canCreateVote():Boolean{
        val groups:List<Group> = dbService.loadGroupsSorted()
        var canCreate = false
        if(!groups.isEmpty()){
            canCreate = groups.any { group -> group.permissions.contains(GroupPermission.CREATE_GROUP_VOTE) }
        }
        return canCreate
    }


    fun setMeetingSubject(subject: String) {
        (task as Meeting).setName(subject)
    }

    fun setTaskDate(t: ActionType, date: Long?) {
        when (t) {
            CreateActionPresenter.ActionType.Meeting -> {
                (task as Meeting).deadlineMillis = date!!
                (task as Meeting).setCreatedDate(System.currentTimeMillis())
            }
            CreateActionPresenter.ActionType.Vote -> {
                (task as Vote).deadlineMillis = date!!
                (task as Vote).setCreatedDate(System.currentTimeMillis())
            }
            CreateActionPresenter.ActionType.Todo -> {
                (task as Todo).deadlineMillis = date!!
                (task as Todo).setCreatedDate(System.currentTimeMillis())
            }
        }
    }

    fun setMeetingLocation(location: String) {
        (task as Meeting).locationDescription = location
    }

    fun setVoteOptions(options: List<String>) {
        (task as Vote).setVoteOptions(options)
    }

    fun setTodoSubject(subject: String) {
        (task as Todo).setSubject(subject)
    }

    fun setVoteSubject(subject: String) {
        (task as Vote).setSubject(subject)
    }

    fun setTodoType(type: String) {
        (task as Todo).setTodoType(type)
    }

    fun setTodoResponseTag(responseTag: String) {
        (task as Todo).responseTag = responseTag
    }

    fun setGroupName(groupName: String) {
        (group as Group).name = groupName
    }

    fun setGroupDescription(description: String) {
        (group as Group).setDescription(description)
    }

    fun setVoteDescription(description: String) {
        (task as Vote).setDescription(description)
    }

    fun setGroupUid(group: Group) {
        if (alert != null) {
            alert!!.alertType = LiveWireAlert.TYPE_GENERIC
            alert!!.groupUid = group.uid
        } else {
            task!!.parentUid = group.uid
        }
    }

    fun takePhoto() {
        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s ->
                    val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, s)
                    Timber.e("mediaFile stored and retrieved, = " + mediaFile!!)
                    // for some reason, sometimes it comes back null ...
                    Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.contentProviderPath))
                    currentMediaFileUid = s
                    view.cameraForResult(mediaFile.contentProviderPath, s)
                }) { throwable ->
                    Timber.e(throwable, "Error creating file")
                    view.showErrorSnackbar(R.string.error_file_creation)
                })
    }

    fun pickFromGallery() {
        disposableOnDetach(view.ensureWriteExteralStoragePermission().flatMapSingle { aBoolean ->
            when {
                aBoolean -> mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
                else -> throw Exception("Permission not granted")
            }
        }.subscribeOn(io()).observeOn(main()).subscribe({ s ->
            currentMediaFileUid = s
            view.pickFromGallery()
        }, { it.printStackTrace() }))
    }

    fun setLongDescription(description: String) {
        if (!TextUtils.isEmpty(description)) {
            alert!!.description = description
        }
    }

    fun cameraResult() {
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid!!)
                .doOnError( { this.handleMediaError(it) })
                .subscribe { _ -> alert!!.mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid!!) })
    }

    fun handlePickResult(data: Uri) {
        disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid!!, data).subscribeOn(Schedulers.io())
                .doOnError({ this.handleMediaError(it) })
                .subscribe({ s -> alert!!.mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid!!) }, { this.handleMediaError(it) }))
    }

    fun setHeadline(headline: String) {
        alert!!.headline = headline
    }

    private fun handleMediaError(throwable: Throwable) {
        view.closeProgressBar()
        Timber.e(throwable)
        view.showErrorSnackbar(R.string.error_lwire_alert_media_error)
    }

    interface CreateActionView : GrassrootView {
        fun ensureWriteExteralStoragePermission(): Observable<Boolean>

        fun uploadSuccessfull(meeting: GrassrootEntityType)

        fun closeScreen()

        fun proceedWithRender(group: Group?)

        fun cameraForResult(contentProviderPath: String, s: String)

        fun videoForResult(contentProviderPath: String, s: String)

        fun pickFromGallery()
    }

    fun handleSpeech(data: Uri) {
        view.showProgressBar()
        networkService.uploadSpeech(MediaRecorderWrapper.SAMPLING_RATE, true, data.path).subscribeOn(io()).observeOn(main()).subscribe({ response ->
            view.closeProgressBar()
        }, {
            it.printStackTrace()
            view.closeProgressBar()
        })
    }

    fun handleVideo(data: Uri) {
        mediaService.storeMediaFile(File(data.path).absolutePath).subscribe({ uid ->
            currentMediaFileUid = uid
            alert!!.mediaFile = dbService.loadObjectByUid(MediaFile::class.java, uid)
        }, {handleMediaError(it)})
    }

}
