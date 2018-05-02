package za.org.grassroot2.presenter.activity

import org.greenrobot.eventbus.EventBus

import java.util.Collections

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.RequestMapper
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.GrassrootView


class GroupDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BasePresenter<GroupDetailsPresenter.GroupDetailsView>() {

    private var groupUid: String? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    fun loadData() {
        disposableOnDetach(databaseService.load(Group::class.java, groupUid!!).subscribeOn(io()).observeOn(main()).subscribe({ group ->
            if (view != null) {
                Timber.d("in GroupDetailsPresenter. View is not null.")
                // if (GroupPermissionChecker.hasCreatePermission(group)) {
                    view.displayFab()
                    Timber.d("User has create groups permissions. Proceed to the get down.")
                // }
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

    fun inviteContacts(contacts: List<Contact>) {
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

    interface GroupDetailsView : GrassrootView {
        fun render(group: Group)
        fun emptyData()
        fun displayFab()
    }

    class TasksUpdatedEvent
}
