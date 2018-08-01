package za.org.grassroot2.presenter.fragment


import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.view.FragmentView

class GroupFragmentPresenter @Inject
constructor(private val databaseService: DatabaseService,
            private val userDetailsService: UserDetailsService,
            private val mediaService: MediaService) : BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView>() {
    private var firstSyncNotCompleted: Boolean = false
    private var currentMediaFileUid: String? = null

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
                    {groupUid ->
                        view.openSelectImageDialog()
                        Timber.d("Image for Group with uid %s was clicked ------------>>>>>>",groupUid)},
                    {it.printStackTrace()}
                )
            )
        }
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

                            }
                        },
                        {
                            Timber.d(it)
                        }
                )
        )
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
    }
}
