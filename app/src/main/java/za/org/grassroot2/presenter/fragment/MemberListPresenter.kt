package za.org.grassroot2.presenter.fragment


import android.text.TextUtils
import io.reactivex.Observable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Membership
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.view.FragmentView
import java.util.*
import javax.inject.Inject

class MemberListPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BaseFragmentPresenter<MemberListPresenter.MemberListFragmentView>() {

    private var groupUid: String? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    override fun onViewCreated() {
        loadMembers()
        disposableOnDetach(view.searchInputChanged().observeOn(main()).subscribe({ searchQuery ->
            view.filterData(searchQuery)
        }))
    }

    fun refreshMembers() {
        // userDetailsService.requestSync()
    }

    private fun loadMembers() {
        disposableOnDetach(databaseService.loadMembersForGroup(groupUid!!).subscribeOn(io()).observeOn(main())
                .subscribe({ members ->
                    Collections.sort(members) { m1, m2 ->
                        val m1EmptyOrDigits = TextUtils.isEmpty(m1.displayName) || TextUtils.isDigitsOnly(m1.displayName)
                        val m2EmptyOrDigits = TextUtils.isEmpty(m2.displayName) || TextUtils.isDigitsOnly(m2.displayName)
                        if (m1EmptyOrDigits == m2EmptyOrDigits) {
                            m1.displayName.compareTo(m2.displayName)
                        } else {
                            if (m1EmptyOrDigits) 1 else -1
                        }
                    }
                    view.render(members)
                    disposableOnDetach(view.itemClick().subscribe({ m -> view.displayMemberDialog(m) }, { it.printStackTrace() }))
                }, { it.printStackTrace() }))
    }

    // todo : actually hook this up
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun syncComplete(e: SyncAdapter.SyncCompletedEvent) {
        onViewCreated()
        view.stopRefreshing()
    }

    interface MemberListFragmentView : FragmentView {
        fun render(members: List<Membership>)
        fun itemClick(): Observable<Membership>
        fun displayMemberDialog(member: Membership)
        fun stopRefreshing()
        fun searchInputChanged() : Observable<String>
        fun filterData(searchQuery: String)
    }
}
