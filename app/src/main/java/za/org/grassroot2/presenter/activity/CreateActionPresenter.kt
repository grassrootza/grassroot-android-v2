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
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.util.MediaRecorderWrapper
import za.org.grassroot2.view.GrassrootView
import java.io.File
import java.util.*
import javax.inject.Inject


class CreateActionPresenter @Inject
constructor(private val networkService: NetworkService, private val dbService: DatabaseService, private val mediaService: MediaService) : BasePresenter<CreateActionPresenter.CreateActionView>() {

    var task: Task? = null
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
        Meeting, LivewireAlert, Vote, Todo
    }

    fun initTask(type: ActionType) {
        when (type) {
            CreateActionPresenter.ActionType.Meeting -> {
                task = Meeting()
                task!!.uid = UUID.randomUUID().toString()
            }
            CreateActionPresenter.ActionType.LivewireAlert -> alert = LiveWireAlert()
        }
    }

    fun createMeeting() {
        view.showProgressBar()
        disposableOnDetach(networkService.createTask(task!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ task ->
            view.closeProgressBar()
            view.uploadSuccessfull(GrassrootEntityType.MEETING)
        }) { throwable ->
            view.closeProgressBar()
            view.uploadSuccessfull(GrassrootEntityType.MEETING)
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

    fun setMeetingDate(date: Long?) {
        (task as Meeting).deadlineMillis = date!!
        (task as Meeting).setCreatedDate(System.currentTimeMillis())
    }

    fun setMeetingLocation(location: String) {
        (task as Meeting).locationDescription = location
    }

    fun setSubject(subject: String) {
        (task as Meeting).setSubject(subject)
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
