package za.org.grassroot2.presenter

import android.net.Uri
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.view.MeView
import java.io.File

class MePresenter(private val dbService: DatabaseService,
                  private val mediaService: MediaService,
                  private val grassrootUserApi: GrassrootUserApi) : BaseFragmentPresenter<MeView>() {


    private lateinit var currentMediaFileUid: String

    private lateinit var userProfile: UserProfile

    override fun onViewCreated() {

        this.userProfile = dbService.loadUserProfile()!!
        view.displayUserData(userProfile)
    }


    fun takePhoto() {
        disposableOnDetach(
                mediaService
                        .createFileForMedia("image/jpeg", MediaFile.FUNCTION_USER_PROFILE_PHOTO)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ s ->
                            val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, s)
                            Timber.e("mediaFile stored and retrieved, = " + mediaFile!!)
                            // for some reason, sometimes it comes back null ...
                            Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.contentProviderPath))
                            currentMediaFileUid = s
                            view.cameraForResult(mediaFile.contentProviderPath, s)
                        }, { throwable ->
                            Timber.e(throwable, "Error creating file")
//                    view.showErrorSnackbar(R.string.error_file_creation)
                        }))
    }

    fun pickFromGallery() {

        disposableOnDetach(view.ensureWriteExteralStoragePermission().flatMapSingle<String> { aBoolean ->
            if (aBoolean) {
                mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_USER_PROFILE_PHOTO)
            } else throw Exception("Permission not granted")
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { s ->
                            currentMediaFileUid = s
                            view.pickFromGallery()
                        },
                        { it.printStackTrace() }
                )
        )
    }


    fun cameraResult() {
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid)
                .doOnError({ this.handleMediaError(it) })
                .subscribe { s ->
                    val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid)
                    if (mediaFile != null)
                        uploadProfilePhoto(mediaFile)
                    println("mediaFile: $mediaFile")
                })
    }

    fun handlePickResult(data: Uri) {
        view.showProgressBar()
        disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid, data).subscribeOn(Schedulers.io())
                .doOnError({ this.handleMediaError(it) })
                .subscribe(
                        { s ->
                            val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid)
                            if (mediaFile != null)
                                uploadProfilePhoto(mediaFile)
                        },
                        { this.handleMediaError(it) }))
    }

    fun updateProfileData(displayName: String, phoneNumber: String, email: String) {

        view.showProgressBar()
        grassrootUserApi.updateProfileData(displayName, phoneNumber, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            val authData = result.data
                            println(authData)

                        },
                        { Timber.e(it) }
                )
    }

    private fun handleMediaError(throwable: Throwable) {
        view.closeProgressBar()
        Timber.e(throwable)
//        view.showErrorSnackbar(R.string.error_lwire_alert_media_error)
    }


    private fun uploadProfilePhoto(mediaFile: MediaFile) {

        mediaFile.initUploading()
        dbService.storeObject(MediaFile::class.java, mediaFile)

        val fileMultipart = getFileMultipart(mediaFile, "photo")

        disposableOnDetach(
                grassrootUserApi.uploadProfilePhoto(fileMultipart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            println("Upload result: $result")
                            view.closeProgressBar()
                            if (result.isSuccessful) {
                                view.invalidateProfilePicCache(userProfile.uid)
                                println(mediaFile.contentProviderPath)
                            } else
                                println("Upload result error:" + result.errorBody())
                        },
                        { error ->
                            println("Upload erorr: $error")
                            view.closeProgressBar()
                        }
                )
        )

//
//                .flatMap(successHandler(mediaFile))
//                .onErrorResumeNext(resumeHandler(mediaFile))
//                .concatMap { uploadResult ->
//
//                    if (uploadResult.uploadException == null) {
//                        mediaFile.haltUploading(true)
//                        mediaFile.isSentUpstream = true
//                        mediaFile.serverUid = uploadResult.serverUid
//                    } else {
//                        mediaFile.haltUploading(false)
//                    }
//                    dbService.storeObject(MediaFile::class.java, mediaFile)
//                    Observable.just(uploadResult)
//                }
    }

    private fun getFileMultipart(mediaFile: MediaFile, paramName: String): MultipartBody.Part? {
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