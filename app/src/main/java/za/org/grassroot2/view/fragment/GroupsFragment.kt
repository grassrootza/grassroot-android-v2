package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_groups.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.view.activity.CreateActionActivity
import za.org.grassroot2.view.activity.GroupDetailsActivity
import za.org.grassroot2.view.adapter.GroupsAdapter
import javax.inject.Inject

class GroupsFragment : GrassrootFragment(), GroupFragmentPresenter.GroupFragmentView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_groups

    override fun stopRefreshing() {
        refreshLayout.isRefreshing = false
    }

    @Inject lateinit internal var presenter: GroupFragmentPresenter
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setTitle(R.string.title_groups)
        fab.setOnClickListener { CreateActionActivity.startFromHome(activity as AppCompatActivity) }
        refreshLayout.setOnRefreshListener { presenter.refreshGroups() }
        presenter.attach(this)
        presenter.onViewCreated()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.fragment_groups)
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
        groupsAdapter = GroupsAdapter(activity!!, groups)
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
        activity?.let { GroupDetailsActivity.start(it, groupUid) }
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
