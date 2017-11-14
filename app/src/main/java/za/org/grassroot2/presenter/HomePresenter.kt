package za.org.grassroot2.presenter

import android.location.Location
import android.widget.Filter
import io.reactivex.Observable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.language.NluIntent
import za.org.grassroot2.model.language.NluResponse
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.account.SyncAdapter
import za.org.grassroot2.view.FragmentView
import za.org.grassroot2.view.activity.CreateActionActivity
import javax.inject.Inject

class HomePresenter @Inject
constructor(private val locationManager: LocationManager, private val dbService: DatabaseService, private val networkService: NetworkService) : BaseFragmentPresenter<HomePresenter.HomeView>() {

    private val testLat = -26.1925350
    private val testLong = 28.0373235
    private var currentAlerts: List<LiveWireAlert> = listOf()
    private var currentTasks: List<Task> = listOf()
    private var currentPublicMeetings: List<AroundEntity> = listOf()
    private var homeItems: MutableList<HomeFeedItem> = mutableListOf()

    // todo: make this work via Rx observable instead
    // todo: have some common list of intent beginnings (first static, in time pulled from NLU)
    override fun onViewCreated() {
        disposableOnDetach(view.searchInputChanged().observeOn(main()).subscribe({ searchQuery ->
            view.filterData(searchQuery, Filter.FilterListener { p0 ->
                if (p0 == 0 && searchQuery.length >= 3) {
                    seekIntentInSearch(searchQuery)
                }
            })
        }))
    }

    private fun seekIntentInSearch(inputText: String) {
        Timber.d("no filter results, checking for intent in text: {}", inputText)
        disposableOnDetach(networkService.seekIntentInText(inputText).observeOn(main()).subscribeOn(io()).subscribe({ t: NluResponse? ->
            Timber.d("nluResponse: {}", t)
            if (t?.intent != null && t.intent.getActionEquivalent() != R.id.unknownIntent) {
                // todo : have some prompt that gives user chance to confirm
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
        getTasks()
    }

    private fun getTasks() {
        disposableOnDetach(dbService.loadAllTasksSorted().subscribeOn(io()).observeOn(main()).subscribe({ tasks ->
            currentTasks = tasks.toList()
            prepareAndRenderItems()
        }, { t -> t.printStackTrace() }))
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
        fun filterData(searchQuery: String, listener: Filter.FilterListener)
        fun stopRefreshing()
        fun initiateCreateAction(actionToInitiate: Int)
    }

}