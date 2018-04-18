package za.org.grassroot2.view.fragment

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Lazy
import dagger.Provides
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.PendingTodoDTO
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.presenter.activity.HomePresenter
import za.org.grassroot2.rxbinding.RxTextView
import za.org.grassroot2.view.activity.*
import za.org.grassroot2.view.adapter.HomeAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFragment : GrassrootFragment(), HomePresenter.HomeView {

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter.attach(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun displayAlert(pending: PendingTodoDTO) {
        Timber.d("This is what I got: %s", pending.toString())
        val alert = AlertDialog.Builder(activity)
        var voteChoice: EditText? = null

        // Builder
        with(alert) {
            setTitle("Group: "+pending.parentName)
                    .setMessage("Created by: " + pending.creatorName)
                    .setMessage(pending.title)
        }
        // Dialog
        val dialog = alert.create()
        dialog.setView(voteChoice)
        dialog.show()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeItemList.layoutManager = LinearLayoutManager(activity)
        homeItemList.adapter = adapter
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        fab.setOnClickListener({ _ -> CreateActionActivity.startFromHome(activity) })
        toolbar.setTitle(R.string.title_home)
        refreshLayout.setOnRefreshListener { reloadView() }
        presenter.onViewCreated()
        refreshView()
    }

    override fun initiateCreateAction(actionToInitiate: Int) {
        Timber.d("initiating create action activity, with actionToInitiate ... " + actionToInitiate)
        CreateActionActivity.startOnAction(activity, actionToInitiate, null)
    }

    private fun refreshView() {
        presenter.loadHomeItems()
        requestLocation()
    }

    private fun reloadView() {
        presenter.reloadHomeItems()
        Timber.d("Positive ping at location alpha")
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
        adapter.setData(tasks)
    }

    override fun listItemClick(): Observable<HomeFeedItem> = adapter.viewClickObservable

    override fun openMeetingDetails(meeting: Meeting) {
        MeetingDetailsActivity.start(activity, meeting.uid)
    }

    override fun openVoteDetails(vote: Vote) {
        VoteDetailsActivity.start(activity, vote.uid)
    }

    override fun openTodoDetails(todo: Todo) {
        TodoDetailsActivity.start(activity, todo.uid)
    }
}
