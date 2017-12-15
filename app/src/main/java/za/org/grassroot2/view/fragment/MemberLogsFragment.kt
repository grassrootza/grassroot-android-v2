package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_member_list.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.MembershipLog
import za.org.grassroot2.presenter.fragment.MemberLogsPresenter
import za.org.grassroot2.rxbinding.RxTextView
import za.org.grassroot2.view.adapter.MemberLogsAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MemberLogsFragment : GrassrootFragment(), MemberLogsPresenter.MemberLogsFragmentView {

    @Inject lateinit internal var presenter: MemberLogsPresenter
    private lateinit var memberLogsAdapter: MemberLogsAdapter

    private var groupUid: String? = null

    override fun getLayoutResourceId(): Int = R.layout.fragment_member_log_list

    override fun searchInputChanged(): Observable<String> =
            RxTextView.textChanges(searchInput).debounce(300, TimeUnit.MILLISECONDS).map { t -> t.toString() }

    override fun filterData(searchQuery: String) {
        memberLogsAdapter.filter.filter(searchQuery)
    }

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

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun render(memberLogs: List<MembershipLog>) {
        memberRecyclerView.visibility = View.VISIBLE
        memberRecyclerView.layoutManager = LinearLayoutManager(activity)
        memberLogsAdapter = MemberLogsAdapter(activity, memberLogs)
        memberRecyclerView.adapter = memberLogsAdapter
    }

    companion object {

        val GROUP_UID_EXTRA_FIELD = "group_uid"

        fun newInstance(groupUid: String): MemberLogsFragment {
            val fragment = MemberLogsFragment()
            val args = Bundle()
            args.putString(GROUP_UID_EXTRA_FIELD, groupUid)
            fragment.arguments = args
            return fragment
        }
    }

}