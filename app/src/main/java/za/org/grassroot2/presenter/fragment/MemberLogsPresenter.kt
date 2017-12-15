package za.org.grassroot2.presenter.fragment


import io.reactivex.Observable
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MembershipLog
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.FragmentView
import javax.inject.Inject

class MemberLogsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BaseFragmentPresenter<MemberLogsPresenter.MemberLogsFragmentView>() {

    private var groupUid: String? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    override fun onViewCreated() {
        loadMemberLogs()
        disposableOnDetach(view.searchInputChanged().observeOn(main()).subscribe({ searchQuery ->
            view.filterData(searchQuery)
        }))
    }

    fun refreshMembers() {
        // userDetailsService.requestSync()
    }

    private fun loadMemberLogs() {
        disposableOnDetach(databaseService.loadMemberLogsForGroup(groupUid!!).subscribeOn(io()).observeOn(main())
                .subscribe({ members ->
                    view.render(members)
                }, { it.printStackTrace() }))
    }

    interface MemberLogsFragmentView : FragmentView {
        fun render(memberLogs: List<MembershipLog>)
        fun searchInputChanged() : Observable<String>
        fun filterData(searchQuery: String)
    }
}
