package za.org.grassroot2.presenter.activity

import android.content.OperationApplicationException
import android.content.SyncResult
import android.location.Location
import android.os.RemoteException
import io.reactivex.Observable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.exception.ServerUnreachableException
import za.org.grassroot2.model.language.NluResponse
import za.org.grassroot2.model.task.*
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.view.FragmentView
import java.util.HashMap
import javax.inject.Inject
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.view.fragment.HomeFragment
import java.io.IOException

class HomePresenter @Inject
constructor(private val locationManager: LocationManager, private val dbService: DatabaseService, private val networkService: NetworkService,
            private val userDetailsService: UserDetailsService ) : BaseFragmentPresenter<HomePresenter.HomeView>() {

    private val testLat = -26.1925350
    private val testLong = 28.0373235
    private var currentAlerts: List<LiveWireAlert> = listOf()
    private var currentTasks: List<Task> = listOf()
    private var currentPublicMeetings: List<AroundEntity> = listOf()
    private var homeItems: MutableList<HomeFeedItem> = mutableListOf()

    override fun onViewCreated() {
        disposableOnDetach(view.listItemClick().subscribe({ m ->
            if (m is Meeting) {
                view.openMeetingDetails(m)
            } else if (m is Vote) {
                view.openVoteDetails(m)
            } else if (m is Todo) {
                view.openTodoDetails(m)
            }
        }, { t -> t.printStackTrace() }))
        disposableOnDetach(view.searchInputChanged().observeOn(main()).subscribe({ searchQuery ->
            view.filterData(searchQuery)
        }))
        disposableOnDetach(view.searchInputDone().observeOn(main()).subscribe({ searchQuery ->
            seekIntentInSearch(searchQuery)
        }))
    }

    private fun seekIntentInSearch(inputText: String) {
        disposableOnDetach(networkService.seekIntentInText(inputText).observeOn(main()).subscribeOn(io()).subscribe({ t: NluResponse? ->
            if (t?.intent != null && t.intent.getActionEquivalent() != R.id.unknownIntent) {
                // note : consider a prompt that gives user chance to confirm this is what they want
                view.initiateCreateAction(t.intent.getActionEquivalent())
            }
        }, { t -> t.printStackTrace() }))
    }

    fun getAlertsAround() {
        val l = Location("")
        l.latitude = testLat
        l.longitude = testLong
        disposableOnDetach(locationManager.currentLocation.subscribe({ location ->
            loadAlertsAround(l)
            getPublicMeetings(l)
        }, { t ->
            t.printStackTrace()
            view.stopRefreshing()
        }))
    }

    private fun getPublicMeetings(location: Location) {
        disposableOnDetach(dbService.loadPublicMeetings().observeOn(main()).subscribeOn(io()).subscribe({ meetings ->
            if (!meetings.isEmpty()) {
                currentPublicMeetings = meetings.toList()
                prepareAndRenderItems()
            } else {
                disposableOnDetach(networkService.getAllAround(location.longitude, location.latitude, 5000).observeOn(main()).subscribeOn(io()).subscribe({ t ->
                    currentPublicMeetings = t.data?.filter { aroundEntity -> aroundEntity.type == GrassrootEntityType.MEETING } ?: listOf()
                    if (!currentPublicMeetings.isEmpty()) {
                        prepareAndRenderItems()
                    }
                }, { t -> t.printStackTrace() }))
            }
        }, { t -> t.printStackTrace() }))
    }

    private fun loadAlertsAround(location: Location) {
        disposableOnDetach(networkService.getAlertsAround(location.longitude, location.latitude, 5000).subscribeOn(io()).observeOn(main()).subscribe({ alerts ->
            currentAlerts = alerts.toList()
            prepareAndRenderItems()
        }, { t -> t.printStackTrace() }))
    }

    fun loadHomeItems() {
        var currentTask: PendingResponseDTO = PendingResponseDTO()
        getTasks()
        Timber.d("About to run network request for pending todos")
        disposableOnDetach(networkService.fetchPendingResponses()
                .subscribeOn(io()).observeOn(main()).subscribe({ task ->
                    currentTask = task
                    if (task.hasPendingResponse != false) {
                        Timber.d("Pending response detected. Looks like: %s", task.toString())
                        view.displayAlert(currentTask)
                    } else {
                        Timber.e("No pending response detected. Moving on.")
                    }
                }, { t -> t.printStackTrace() }))
        Timber.d("Network request sent?")
        //view.displayAlert(currentTask)
    }

    private fun getTasks() {
        disposableOnDetach(dbService.loadAllTasksSorted().subscribeOn(io()).observeOn(main()).subscribe({ tasks ->
            currentTasks = tasks.toList()
            prepareAndRenderItems()
        }, { t -> t.printStackTrace() }))
    }

    fun reloadHomeItems() {
        refreshTasks()
    }

    private fun refreshTasks() {
        Timber.d("Positive ping at location beta")
        networkService.downloadTaskMinimumInfo().subscribeOn(io()).flatMap { tasksMin ->
            dbService.storeTasks(tasksMin)
            val uids = HashMap<String, String>()
            for (t in tasksMin) {
                uids.put(t.uid, t.type.name)
                Timber.d("The contents of variable t are: %s", t)
            }
            networkService.getTasksByUids(uids)
        }.observeOn(main()).subscribe({ tasksFull ->
            dbService.storeTasks(tasksFull)
            prepareAndRenderItems()
        })
        Timber.d("location beta exit ping.")
    }

    private fun prepareAndRenderItems() {
        homeItems.clear()
        homeItems.addAll(currentTasks)
        homeItems.addAll(currentAlerts)
        homeItems.addAll(currentPublicMeetings)
        homeItems.sortByDescending { homeFeedItem -> homeFeedItem.date() }
        view.render(homeItems)
        view.stopRefreshing()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun syncComplete(e: SyncAdapter.SyncCompletedEvent) {
        loadHomeItems()
    }

    interface HomeView : FragmentView {
        fun render(tasks: List<HomeFeedItem>)
        fun searchInputChanged() : Observable<String>
        fun searchInputDone() : Observable<String>
        fun filterData(searchQuery: String)
        fun stopRefreshing()
        fun initiateCreateAction(actionToInitiate: Int)
        fun listItemClick() : Observable<HomeFeedItem>
        fun openMeetingDetails(meeting: Meeting)
        fun openVoteDetails(vote: Vote)
        fun openTodoDetails(todo: Todo)
        fun displayAlert(pending: PendingResponseDTO)
    }

}