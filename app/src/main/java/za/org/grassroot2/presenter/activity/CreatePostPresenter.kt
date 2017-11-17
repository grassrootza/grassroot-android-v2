package za.org.grassroot2.presenter.activity

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject


class CreatePostPresenter @Inject
constructor(private val networkService: NetworkService, private val dbService: DatabaseService, private val mediaService: MediaService) : BasePresenter<CreatePostPresenter.CreatePostView>() {

    var currentMediaFileUid: String? = null
    lateinit var postSubject: String

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
            if (aBoolean) {
                return@flatMapSingle mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
            }
            throw Exception("Permission not granted")
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s ->
                    currentMediaFileUid = s
                    view.pickFromGallery()
                }, { it.printStackTrace() }))
    }

    fun cameraResult() {
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid!!)
                .doOnError({ this.handleMediaError(it) })
                .subscribe { _ ->
                    //                    liveWireAlert.setMediaFile(dbService.loadObjectByUid(MediaFile.class, currentMediaFileUid));
                })
    }

    fun handlePickResult(data: Uri) {
        disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid!!, data).subscribeOn(Schedulers.io())
                .doOnError({ this.handleMediaError(it) })
                .subscribe({ s -> }, { this.handleMediaError(it) }))
    }

    private fun handleMediaError(throwable: Throwable) {
        view.closeProgressBar()
        Timber.e(throwable)
        view.showErrorSnackbar(R.string.error_lwire_alert_media_error)
    }

    interface CreatePostView : GrassrootView {
        fun ensureWriteExteralStoragePermission(): Observable<Boolean>

        fun uploadSuccessfull()

        fun closeScreen()

        fun cameraForResult(contentProviderPath: String, s: String)

        fun videoForResult(contentProviderPath: String, s: String)

        fun pickFromGallery()
    }

    fun setSubject(subject: String) {
        postSubject = subject
    }

    fun createPost(meetingUid: String) {
        view.showProgressBar()
        val subscriber = Consumer<Response<Void>> {t ->
            view.uploadSuccessfull()
            view.closeProgressBar()
        }
        if (currentMediaFileUid != null) {
            dbService.load(MediaFile::class.javaObjectType, currentMediaFileUid!!)
                    .flatMapObservable { media -> networkService.uploadMeetingPost(meetingUid, postSubject, media) }.subscribeOn(io())
                    .observeOn(main()).subscribe(subscriber, Consumer { t ->
                handleNetworkUploadError(t)
                view.closeProgressBar()
            })
        } else {
            networkService.uploadMeetingPost(meetingUid, postSubject, null).subscribeOn(io()).observeOn(main())
                    .subscribe(subscriber, Consumer { t ->
                handleNetworkUploadError(t)
                view.closeProgressBar()
            })
        }
    }

}
