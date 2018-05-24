package za.org.grassroot2.presenter.fragment


import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import javax.inject.Inject

import io.reactivex.Observable
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.view.FragmentView

class GroupFragmentPresenter @Inject
constructor(private val databaseService: DatabaseService, private val userDetailsService: UserDetailsService) : BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView>() {
    private var firstSyncNotCompleted: Boolean = false

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
    }
}
