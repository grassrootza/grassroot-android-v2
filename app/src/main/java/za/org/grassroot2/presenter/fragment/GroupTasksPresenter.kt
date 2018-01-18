package za.org.grassroot2.presenter.fragment

import org.greenrobot.eventbus.Subscribe

import java.util.Collections

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.presenter.activity.GroupDetailsPresenter
import za.org.grassroot2.view.FragmentView

class GroupTasksPresenter @Inject
constructor() : BaseFragmentPresenter<GroupTasksPresenter.AllFragmentView>() {

    private var databaseService: DatabaseService? = null
    private var groupUid: String? = null
    private var type: GrassrootEntityType? = null

    override fun onViewCreated() {}

    fun loadTasks() {
        disposableOnDetach(databaseService!!.loadTasksForGroup(groupUid!!, type).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ tasks ->
            if (!tasks.isEmpty()) {
                Collections.sort(tasks) { o1, o2 -> if (o1.deadlineMillis < o2.deadlineMillis) 1 else if (o1.deadlineMillis > o2.deadlineMillis) -1 else 0 }
                view.render(tasks)
            } else {
                view.empty()
            }
        }, { it.printStackTrace() }))
    }

    fun init(groupUid: String, type: GrassrootEntityType?) {
        this.groupUid = groupUid
        this.type = type
        disposableOnDetach(view.taskSelected().subscribe({ task -> taskClick(task) }, { it.printStackTrace() }))
    }

    private fun taskClick(task: Task) {
        if (task is Meeting) {
            view.showMeetingDetails(task.uid)
        } else if (task is Vote) {
            view.showVoteDetails(task.uid)
        }
    }

    @Inject
    fun setDatabaseService(databaseService: DatabaseService) {
        this.databaseService = databaseService
    }

    interface AllFragmentView : FragmentView {
        fun render(tasks: List<Task>)
        fun taskSelected(): Observable<Task>
        fun empty()
        fun showMeetingDetails(uid: String)
        fun showVoteDetails(uid: String)
    }

    @Subscribe
    fun refreshData(e: GroupDetailsPresenter.TasksUpdatedEvent) {
        loadTasks()
    }

}
