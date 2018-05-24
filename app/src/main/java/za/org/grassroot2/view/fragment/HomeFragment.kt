package za.org.grassroot2.view.fragment

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
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
import za.org.grassroot2.model.task.*
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

    override fun displayAlert(pending: PendingResponseDTO) {
        Timber.d("This is what I got: %s", pending.toString())

        // val dialogBuilder = AlertDialog.Builder(activity)
        // val inflater = this.layoutInflater
        // val dialogView = inflater.inflate(R.layout.fragment_pending_todo, null)
        // dialogBuilder.setView(dialogView)

        // val creator = dialogView.findViewById(R.id.creatorField) as TextView
        // creator.setText("Created by: "+pending.creatorName)

        // val pendingDescription = dialogView.findViewById(R.id.contentField) as TextView
        // pendingDescription.setText(pending.title)

        // val alertDialog = dialogBuilder.create()
        // alertDialog.show()

        val dialogBuilder = AlertDialog.Builder(activity)

        dialogBuilder.setTitle("Group: "+pending.parentName)

        var details: Array<String> = arrayOf("Created by: "+pending.creatorName, pending.title)

        with(dialogBuilder) {
            setTitle("Group: "+pending.parentName)
                    .setItems(details, DialogInterface.OnClickListener { dialog, which ->
                        details.elementAt(which)
                    })
        }

        dialogBuilder.setPositiveButton("OPEN",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    openPendingTask(pending)
        }

        })
        dialogBuilder.setNegativeButton("CLOSE", DialogInterface.OnClickListener { dialog, whichButton ->
            //pass
        })

        val b = dialogBuilder.create()
        b.show()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_home
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeItemList.layoutManager = LinearLayoutManager(activity)
        homeItemList.adapter = adapter
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        fab.setOnClickListener({_ ->
            Timber.d("Fab clicked")
            CreateActionActivity.startFromHome(activity)
        })
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

    fun openPendingTask(task: PendingResponseDTO) {
        Timber.d("This is what PendingResponseDTO looks like in HomeFragment: %s", task.toString())
        Timber.d("enityType = %s", task.entityType)
        if (task.entityType == "TODO") {
            TodoDetailsActivity.start(activity, task.entityUid)
        }
        else if (task.entityType == "VOTE") {
            VoteDetailsActivity.start(activity, task.entityUid)
        }
        else if (task.entityType == "VOTE") {
            MeetingDetailsActivity.start(activity, task.entityUid)
        }
    }
}
