package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_groups.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.view.activity.CreateActionActivity
import za.org.grassroot2.view.activity.GroupDetailsActivity
import za.org.grassroot2.view.adapter.GroupsAdapter
import javax.inject.Inject

class GroupsFragment : GrassrootFragment(), GroupFragmentPresenter.GroupFragmentView {

    override fun stopRefreshing() {
        refreshLayout.isRefreshing = false
    }

    @Inject lateinit internal var presenter: GroupFragmentPresenter
    private lateinit var groupsAdapter: GroupsAdapter

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
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.title_groups)
        fab.setOnClickListener { CreateActionActivity.startFromHome(activity) }
        refreshLayout.setOnRefreshListener { presenter.refreshGroups() }
        presenter.attach(this)
        presenter.onViewCreated()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_groups
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun render(groups: List<Group>) {
        groupRecyclerView.visibility = View.VISIBLE
        emptyInfoContainer.visibility = View.GONE
        groupRecyclerView.layoutManager = LinearLayoutManager(activity)
        groupsAdapter = GroupsAdapter(activity, groups)
        val footer = LayoutInflater.from(activity).inflate(R.layout.item_group_footer, null, false)
        groupsAdapter.addFooter(footer)
        groupRecyclerView.adapter = groupsAdapter
    }

    override fun renderEmpty() {
        displayEmptyLayout()
        emptyInfo!!.setText(R.string.no_group_info)
    }

    override fun itemClick(): Observable<String> {
        return groupsAdapter.viewClickObservable
    }

    override fun renderEmptyFailedSync() {
        displayEmptyLayout()
        emptyInfo!!.setText(R.string.sync_problem)
    }

    override fun openDetails(groupUid: String) {
        GroupDetailsActivity.start(activity, groupUid)
    }

    private fun displayEmptyLayout() {
        emptyInfoContainer.visibility = View.VISIBLE
        groupRecyclerView.visibility = View.GONE
    }

    companion object {

        fun newInstance(): GroupsFragment {
            val fragment = GroupsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
