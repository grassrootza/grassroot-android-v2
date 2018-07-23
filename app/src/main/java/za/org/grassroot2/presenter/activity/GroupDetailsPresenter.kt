package za.org.grassroot2.presenter.activity

import android.net.Uri
import org.greenrobot.eventbus.EventBus

import java.util.Collections

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.RequestMapper
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.GrassrootView


class GroupDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService,private val mediaService: MediaService) : BasePresenter<GroupDetailsPresenter.GroupDetailsView>() {

    private var groupUid: String? = null
    private var currentMediaFileUid: String? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    fun loadData() {
        disposableOnDetach(databaseService.load(Group::class.java, groupUid!!).subscribeOn(io()).observeOn(main()).subscribe({ group ->
            if (view != null) {
                Timber.d("in GroupDetailsPresenter. View is not null.")
                if (GroupPermissionChecker.hasCreatePermission(group)) {
                    view.displayFab()
                    Timber.d("User has create groups permissions. Proceed to the get down.")
                }
                view.render(group)
            }
        }, { it.printStackTrace() }))
        disposableOnDetach(networkService.getTasksForGroup(groupUid!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ tasks ->
            if (!tasks.isEmpty()) {
                databaseService.storeTasks(tasks)
                EventBus.getDefault().post(TasksUpdatedEvent())
            } else {
                view.emptyData()
            }
        }, { this.handleNetworkConnectionError(it) }))

    }

    fun inviteContacts(contacts: List<Contact>?) {
        view.showProgressBar()
        disposableOnDetach(networkService.inviteContactsToGroup(groupUid!!, RequestMapper.map(groupUid, contacts)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe({ voidResponse ->
            view.closeProgressBar()
            if (voidResponse.isSuccessful) {
            } else {
                view.showErrorSnackbar(R.string.error_permission_denied)
            }
        }, { this.handleNetworkUploadError(it) }))
    }

    fun inviteContact(name: String, phone: String) {
        val c = Contact()
        c.displayName = name
        c.setPhoneNumber(phone)
        inviteContacts(listOf(c))
    }

    fun takePhoto() {
        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_GROUP_PROFILE_PHOTO).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s ->
                    val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, s)
                    Timber.e("mediaFile stored and retrieved, = " + mediaFile!!)
                    // for some reason, sometimes it comes back null ...
                    Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.contentProviderPath))
                    currentMediaFileUid = s
                    Timber.d("What does this s contain #############  %s",s)
                    Timber.d("What is the group uid here in Presenter ???????????????????????????????????????? %s",groupUid)
                    Timber.d("Media file uid is ------------->>>>>> %s",mediaFile.serverUid)

                    view.cameraForResult(mediaFile.contentProviderPath, s)
                }) { throwable ->
                    Timber.e(throwable, "Error creating file")
                    view.showErrorSnackbar(R.string.error_file_creation)
                })
    }

    /*fun cameraResult(){
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid!!)
                .subscribe({}))
    }*/

    interface GroupDetailsView : GrassrootView {
        fun render(group: Group)
        fun emptyData()
        fun displayFab()
        fun cameraForResult(contentProviderPath: String, s: String)
    }

    class TasksUpdatedEvent
}
