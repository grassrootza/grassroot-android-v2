package za.org.grassroot2.view.fragment

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.PendingResponseDTO
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.presenter.activity.HomePresenter
import za.org.grassroot2.view.activity.CreateActionActivity
import za.org.grassroot2.view.activity.MeetingDetailsActivity
import za.org.grassroot2.view.activity.TodoDetailsActivity
import za.org.grassroot2.view.activity.VoteDetailsActivity
import za.org.grassroot2.view.adapter.HomeAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFragment : GrassrootFragment(), HomePresenter.HomeView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_home

    override fun stopRefreshing() {
        refreshLayout.isRefreshing = false
    }

    override fun searchInputChanged(): Observable<String> =
         RxTextView.textChanges(searchInput).debounce(300, TimeUnit.MILLISECONDS).map { t -> t.toString() }

    override fun searchInputDone(): Observable<String> = RxTextView
            .editorActionEvents(searchInput)
            .filter { event -> event.actionId() == EditorInfo.IME_ACTION_DONE && searchInput.length() > 3}
            .map { event -> searchInput.text.toString() }

    override fun filterData(searchQuery: String) {
        adapter.filter.filter(searchQuery)
    }

    @Inject internal lateinit var presenter: HomePresenter
    @Inject internal lateinit var adapter: HomeAdapter
    @Inject internal lateinit var rxPermissions: Lazy<RxPermissions>

    override fun onInject(activityComponent: ActivityComponent) {
        get().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter.attach(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun displayAlert(pending: PendingResponseDTO) {
        Timber.d("This is what I got: %s", pending.toString())
        Timber.e("Attempting to display dialogView in HomeFragment")

        val dialogBuilder = AlertDialog.Builder(activity!!)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_pending_task, null)
        dialogBuilder.setView(dialogView)

        val entity = dialogView.findViewById(R.id.pending_entity_type) as TextView
        entity.setText(pending.entityType)

        val creator = dialogView.findViewById(R.id.creatorField) as TextView
        creator.text = getString(R.string.created_by_string, pending.creatorName)

        val pendingDescription = dialogView.findViewById(R.id.contentField) as TextView
        pendingDescription.text = pending.title

        (dialogView.findViewById(R.id.pending_task_open) as Button).setOnClickListener { _ ->
            openPendingTask(pending)
        }

        (dialogView.findViewById(R.id.pending_task_close) as Button).setOnClickListener { _ ->
            // pass
            Timber.d("Exiting alertDialog")
        }
        
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fab.setOnClickListener { _ -> CreateActionActivity.startFromHome(activity as AppCompatActivity) }
        toolbar.setTitle(R.string.title_home)
        refreshLayout.setOnRefreshListener { reloadView() }
        presenter.onViewCreated()

        homeItemList.layoutManager = LinearLayoutManager(activity)
        homeItemList.adapter = adapter

        refreshView()
    }

    override fun initiateCreateAction(actionToInitiate: Int) {
        Timber.d("initiating create action activity, with actionToInitiate ... %s", actionToInitiate)
        activity?.let { CreateActionActivity.startOnAction(it, actionToInitiate, null) }
    }

    private fun refreshView() {
        presenter.reloadHomeItems()
        requestLocation()
        presenter.getTasksFromDbAndRender()
    }

    private fun reloadView() {
        presenter.reloadHomeItems()
        requestLocation()
    }

    private fun requestLocation() {
        disposables.add(rxPermissions.get().request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe({ aBoolean ->
            if (aBoolean) {
                presenter.getAlertsAround()
            } else {
                throw Exception("Location permission not granted!")
            }
        }, {t -> t.printStackTrace()}))
    }

    override fun render(tasks: List<HomeFeedItem>) {
        Timber.e("Calling render tasks, etc, with %d items", tasks.size);
        if (alert.visibility == View.VISIBLE) {
            alert.visibility = View.GONE
        }
        refreshLayout.visibility = View.VISIBLE
        homeItemList.visibility = View.VISIBLE
        adapter.setData(tasks)
    }

    override fun listItemClick(): Observable<HomeFeedItem> = adapter.viewClickObservable

    override fun openMeetingDetails(meeting: Meeting) {
        activity?.let { MeetingDetailsActivity.start(it, meeting.uid) }
    }

    override fun openVoteDetails(vote: Vote) {
        activity?.let { VoteDetailsActivity.start(it, vote.uid) }
    }

    override fun openTodoDetails(todo: Todo) {
        activity?.let { TodoDetailsActivity.start(it, todo.uid) }
    }

    fun openPendingTask(task: PendingResponseDTO) {
        Timber.d("This is what PendingResponseDTO looks like in HomeFragment: %s", task.toString())
        Timber.d("enityType = %s", task.entityType)
        if (task.entityType == "TODO") {
            activity?.let { TodoDetailsActivity.start(it, task.entityUid) }
        }
        else if (task.entityType == "VOTE") {
            activity?.let { VoteDetailsActivity.start(it, task.entityUid) }
        }
        else if (task.entityType == "VOTE") {
            activity?.let { MeetingDetailsActivity.start(it, task.entityUid) }
        }
    }
}
