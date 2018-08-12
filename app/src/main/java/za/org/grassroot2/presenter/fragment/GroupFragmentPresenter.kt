package za.org.grassroot2.presenter.fragment


import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.MediaUploadResult
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.view.FragmentView
import java.io.File

class GroupFragmentPresenter @Inject
constructor(private val databaseService: DatabaseService,
            private val userDetailsService: UserDetailsService,
            private val mediaService: MediaService,
            private val grassrootUserApi: GrassrootUserApi) : BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView>() {

    private var firstSyncNotCompleted: Boolean = false
    private var currentMediaFileUid: String? = null
    private lateinit var groupUid:String

    override fun onViewCreated() {
        firstSyncNotCompleted = !userDetailsService.isSyncFailed && !userDetailsService.isSyncCompleted
        if (userDetailsService.isSyncFailed) {
            view.closeProgressBar()
            view.renderEmptyFailedSync()
        } else {
            if (firstSyncNotCompleted) {
                Timber.d("Showing progress bar in GroupFragmentPresenter")
                view.showProgressBar()
            } else {
                view.closeProgressBar()
            }
            loadGroups()
        }
    }

    fun refreshGroups() {
        Timber.e("refresh groups triggered inside groups fragment")
        userDetailsService.requestSync()
    }

    private fun loadGroups() {
        val groups = databaseService.loadGroupsSorted()
        if (groups.isEmpty() && !firstSyncNotCompleted) {
            view.closeProgressBar()
            view.renderEmpty()
        } else {
            view.render(groups)
            disposableOnDetach(view.itemClick().subscribe({ s -> view.openDetails(s) }, { it.printStackTrace() }))

            disposableOnDetach(view.groupImageClick().subscribe(
                    {uid ->
                        groupUid = uid
                        view.openSelectImageDialog()
                        Timber.d("Set image for group with uid ------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> %s ",uid)
                    },
                    {it.printStackTrace()}
                )
            )
        }
    }

    fun getUid():String{
        return groupUid
    }

    fun takePhoto() {
        disposableOnDetach(
                mediaService
                        .createFileForMedia("image/jpeg", MediaFile.FUNCTION_GROUP_PROFILE_PHOTO)
                        .subscribeOn(io())
                        .observeOn(main())
                        .subscribe({ s ->
                            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, s)
                            currentMediaFileUid = s

                            view.cameraForResult(mediaFile!!.contentProviderPath, s)
                        }) { throwable ->
                            Timber.e(throwable, "Error creating file")
                        })
    }

    fun pickFromGallery() {
        disposableOnDetach(view.ensureWriteExteralStoragePermission().flatMapSingle { aBoolean ->
            when {
                aBoolean -> mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_GROUP_PROFILE_PHOTO)
                else -> throw Exception("Permission not granted")
            }
        }.subscribeOn(io()).observeOn(main()).subscribe({ s ->
            currentMediaFileUid = s
            view.pickFromGallery()
        }, { it.printStackTrace() }))
    }

    fun cameraResult(){
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid!!,500,500)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid!!)
                            if (mediaFile != null){
                                uploadProfilePhoto(mediaFile)
                            }
                        },
                        {
                            Timber.d(it)
                        }
                )
        )
    }

    private fun uploadProfilePhoto(mediaFile: MediaFile){
        val fileMultipart = getFileMultipart(mediaFile, "image")
        disposableOnDetach(
                grassrootUserApi.uploadGroupProfilePhoto(groupUid,fileMultipart)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    val mediaUploadResult: MediaUploadResult? = result.body()
                                    setGroupImageUrl(mediaUploadResult?.imageUrl)
                                    view.setImage(mediaUploadResult?.imageUrl)
                                },
                                { error ->
                                    Timber.e(error)
                                }
                        )
        )
    }

    fun setGroupImageUrl(imageUrl:String?){
        val group :Group = databaseService.loadGroup(groupUid) as Group
        group?.profileImageUrl = imageUrl
        databaseService.storeObject(Group::class.java,group)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun syncComplete(e: SyncAdapter.SyncCompletedEvent) {
        onViewCreated()
        view.stopRefreshing()
    }

    interface GroupFragmentView : FragmentView {
        fun render(groups: List<Group>)
        fun renderEmpty()
        fun itemClick(): Observable<String>
        fun renderEmptyFailedSync()
        fun openDetails(groupUid: String)
        fun stopRefreshing()
        fun groupImageClick(): Observable<String>
        fun openSelectImageDialog()
        fun cameraForResult(contentProviderPath: String, s: String)
        fun ensureWriteExteralStoragePermission(): Observable<Boolean>
        fun pickFromGallery()
        fun setImage(imageUrl:String?)
    }
}
