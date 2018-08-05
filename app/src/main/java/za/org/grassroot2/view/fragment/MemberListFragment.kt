package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_member_list.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Membership
import za.org.grassroot2.presenter.fragment.MemberListPresenter
import za.org.grassroot2.view.adapter.MembersAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MemberListFragment : GrassrootFragment(), MemberListPresenter.MemberListFragmentView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_member_list

    @Inject lateinit internal var presenter: MemberListPresenter
    private lateinit var membersAdapter: MembersAdapter

    override fun searchInputChanged(): Observable<String> =
            RxTextView.textChanges(searchInput).debounce(300, TimeUnit.MILLISECONDS).map { t -> t.toString() }

    override fun filterData(searchQuery: String) {
        membersAdapter.filter.filter(searchQuery)
    }

    override fun onInject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_groups, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // fab.setOnClickListener { CreateActionActivity.start(activity, null) }
        refreshLayout.setOnRefreshListener { presenter.refreshMembers() }

        presenter.init(arguments!![GROUP_UID_EXTRA_FIELD] as String)
        presenter.attach(this)
        presenter.onViewCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun render(members: List<Membership>) {
        memberRecyclerView.visibility = View.VISIBLE
        memberRecyclerView.layoutManager = LinearLayoutManager(activity)
        membersAdapter = MembersAdapter(activity!!, members)
        memberRecyclerView.adapter = membersAdapter
    }

    override fun displayMemberDialog(member: Membership) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun itemClick(): Observable<Membership> {
        return membersAdapter.viewClickObservable
    }

    override fun stopRefreshing() {
        refreshLayout.isRefreshing = false
    }

    companion object {

        val GROUP_UID_EXTRA_FIELD = "group_uid"

        fun newInstance(groupUid: String): MemberListFragment {
            val fragment = MemberListFragment()
            val args = Bundle()
            args.putString(GROUP_UID_EXTRA_FIELD, groupUid)
            fragment.arguments = args
            return fragment
        }
    }

}