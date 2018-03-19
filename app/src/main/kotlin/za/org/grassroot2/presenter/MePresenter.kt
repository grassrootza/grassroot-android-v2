package za.org.grassroot2.presenter

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.TokenResponse
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.MeView
import java.io.File

class MePresenter(private val dbService: DatabaseService,
                  private val mediaService: MediaService,
                  private val userDetailsService: UserDetailsService,
                  private val grassrootUserApi: GrassrootUserApi) : BaseFragmentPresenter<MeView>() {

    private lateinit var currentMediaFileUid: String
    private lateinit var userProfile: UserProfile

    override fun onViewCreated() {
        this.userProfile = dbService.loadUserProfile()!! //this will thrown NPE if user gets to this screen, and it has no user profile data stored
        view.displayUserData(userProfile)
    }

    fun logout() {
        disposableOnDetach(userDetailsService.logout(true, true)
                .subscribeOn(io())
                .observeOn(main())
                .subscribe({ b ->
                    view.returnToWelcomeActivity()
                }, { it.printStackTrace() }))
    }

    fun getLanguages(): Observable<Map<String, String>> {
        return grassrootUserApi.fetchLanguages()
    }

    fun takePhoto() {
        disposableOnDetach(
                mediaService
                        .createFileForMedia("image/jpeg", MediaFile.FUNCTION_USER_PROFILE_PHOTO)
                        .subscribeOn(io())
                        .observeOn(main())
                        .subscribe({ s ->
                            val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, s)!!
                            currentMediaFileUid = s
                            view.cameraForResult(mediaFile.contentProviderPath, s)
                        },
                                { throwable ->
                                    view.closeProgressBar()
                                    view.showErrorDialog(R.string.me_error_updating_photo)
                                    Timber.e(throwable, "Error creating file")
                                }
                        ))
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
                        {
                            view.closeProgressBar()
                            view.showErrorDialog(R.string.me_error_updating_photo)
                            it.printStackTrace()
                        }
                )
        )
    }

    fun cameraResult() {
        view.showProgressBar()
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid, 500, 500)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid)
                            if (mediaFile != null)
                                uploadProfilePhoto(mediaFile)
                        },
                        {
                            this.handleMediaError(it)
                        }
                )
        )
    }

    fun handlePickResult(data: Uri) {
        view.showProgressBar()
        disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid, data, 500, 500)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val mediaFile = dbService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid)
                            if (mediaFile != null)
                                uploadProfilePhoto(mediaFile)
                        },
                        {
                            this.handleMediaError(it)
                        }
                )
        )
    }

    fun updateProfileData(displayName: String, phoneNumber: String, email: String, languageCode: String) {
        view.showProgressBar()
        grassrootUserApi.updateProfileData(displayName, phoneNumber, email, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            storeSuccessfulAuthAndProceed(result)
                        },
                        {
                            view.closeProgressBar()
                            view.showErrorDialog(R.string.me_error_updating_data)
                            Timber.e(it)
                        }
                )
    }

    private fun handleMediaError(throwable: Throwable) {
        view.closeProgressBar()
        view.showErrorDialog(R.string.me_error_updating_photo)
        view.closeProgressBar()
        Timber.e(throwable)
    }

    private fun uploadProfilePhoto(mediaFile: MediaFile) {
        view.showProgressBar()
        mediaFile.initUploading()
        dbService.storeObject(MediaFile::class.java, mediaFile)

        val fileMultipart = getFileMultipart(mediaFile, "photo")

        disposableOnDetach(
                grassrootUserApi.uploadProfilePhoto(fileMultipart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            view.closeProgressBar()
                            if (result.isSuccessful) {
                                view.invalidateProfilePicCache(userProfile.uid)
                            }
                        },
                        { error ->
                            view.closeProgressBar()
                            view.showErrorDialog(R.string.me_error_updating_photo)
                            Timber.e(error)
                        }
                )
        )
    }

    private fun getFileMultipart(mediaFile: MediaFile, paramName: String): MultipartBody.Part? {
        return try {
            val file = File(mediaFile.absolutePath)
            val requestFile = RequestBody.create(MediaType.parse(mediaFile.mimeType), file)
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun storeSuccessfulAuthAndProceed(response: RestResponse<TokenResponse>) {
        val tokenAndUserDetails = response.data
        tokenAndUserDetails?.let {
            disposableOnDetach(
                    userDetailsService.storeUserDetails(tokenAndUserDetails.userUid,
                            tokenAndUserDetails.msisdn,
                            tokenAndUserDetails.displayName,
                            tokenAndUserDetails.email,
                            tokenAndUserDetails.languageCode,
                            tokenAndUserDetails.systemRole,
                            tokenAndUserDetails.token)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { upd ->
                                        this.userProfile = upd
                                        view.displayUserData(this.userProfile)
                                        view.closeProgressBar()
                                    },
                                    {
                                        view.closeProgressBar()
                                        view.showErrorDialog(R.string.me_error_updating_data)
                                        Timber.e(it)
                                    }
                            )
            )
        }
    }

    fun isCurrentLanguage(languageCode: String): Boolean {
        return userProfile.languageCode == languageCode
    }

    fun addDisposableOnDetach(disposable: Disposable) {
        disposableOnDetach(disposable)
    }

}