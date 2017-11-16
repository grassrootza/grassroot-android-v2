package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import butterknife.BindView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_group_items.*
import kotlinx.android.synthetic.main.fragment_groups.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.presenter.fragment.GroupTasksPresenter
import za.org.grassroot2.view.activity.MeetingDetailsActivity
import za.org.grassroot2.view.adapter.GroupTasksAdapter

class GroupTasksFragment : GrassrootFragment(), GroupTasksPresenter.AllFragmentView {

    private var oldAfter: Long = 0

    @Inject lateinit var presenter: GroupTasksPresenter
    private lateinit var adapter: GroupTasksAdapter

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter.attach(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_group_items

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        oldAfter = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        allRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        adapter = GroupTasksAdapter(activity, ArrayList(), oldAfter)
        adapter.viewClickObservable
        allRecyclerView!!.adapter = adapter
        presenter.init(arguments.getString(EXTRA_GROUP_UID)!!, arguments.getSerializable(EXTRA_TYPE) as? GrassrootEntityType)
        presenter.loadTasks()
    }

    override fun showMeetingDetails(uid: String) {
        MeetingDetailsActivity.start(activity, uid)
    }

    override fun render(tasks: List<Task>) {
        emptyInfo!!.visibility = View.GONE
        adapter.setData(tasks.toMutableList())
    }

    override fun taskSelected(): Observable<Task> = adapter.viewClickObservable

    override fun empty() {
        emptyInfo!!.visibility = View.VISIBLE
    }

    companion object {

        private val EXTRA_GROUP_UID = "groupUid"
        private val EXTRA_TYPE = "type"

        fun newInstance(groupUid: String?, type: GrassrootEntityType?): Fragment {
            val allFragment = GroupTasksFragment()
            val b = Bundle()
            b.putString(EXTRA_GROUP_UID, groupUid)
            b.putSerializable(EXTRA_TYPE, type)
            allFragment.arguments = b
            return allFragment
        }
    }
}
