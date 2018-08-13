package za.org.grassroot2.presenter.fragment

import android.location.Location
import io.reactivex.Flowable

import java.util.HashSet

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.Group
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.Status
import za.org.grassroot2.view.FragmentView


class AroundMePresenter @Inject
constructor(private val locationManager: LocationManager, private val networkService: NetworkService, private val dbService: DatabaseService) : BaseFragmentPresenter<AroundMePresenter.AroundMeView>() {
    private val testLat = -26.1925350
    private val testLong = 28.0373235

    private var userLocation: Location? = null;
    private val existingLocations = HashSet<String>()
    private var locationSubscription = CompositeDisposable()

    fun getCurrentLocation() {
        val l = Location("")
        l.latitude = testLat
        l.longitude = testLong
        locationSubscription.add(locationManager.currentLocation.subscribe({ location ->
            view.renderLocation(location)
            userLocation = location;
        }, {t -> Timber.e(t) }))
    }

    override fun onViewCreated() {}

    fun loadItemsAround() {
        Timber.e("Fetching items around: default long - %f, default lat - %f, user location: %s", testLong, testLat, userLocation)
        unsubscribeLocation()
        val long = userLocation?.longitude ?: testLong;
        val lat = userLocation?.latitude ?: testLat;
        disposableOnDetach(networkService.getAllAround(testLong, testLat, 5000).flatMap { resource ->
            if (resource.status != Status.ERROR) {
                adjustLocations(resource.data!!)
                Flowable.just(resource.data)
            } else {
                Flowable.just(ArrayList<AroundEntity>())
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ itemsAround -> view.renderItemsAround(itemsAround) }, {t -> t.printStackTrace() }))
    }

    private fun adjustLocations(aroundItems: List<AroundEntity>) {
        aroundItems.forEachIndexed { index, item ->
            var location = getLocationAsString(item)
            if (existingLocations.contains(location)) {
                item.latitude = item.latitude!! + index * OFFSET
                item.longitude = item.longitude!! + index * OFFSET
                location = getLocationAsString(item)
            }
            existingLocations.add(location)
        }
    }

    private fun getLocationAsString(item: AroundEntity): String {
        return item.latitude.toString() + item.longitude.toString()
    }

    fun openGroupDetail(uid: String) {
        dbService.load(Group::class.java, uid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ group -> view.openGroupDetails(group.uid) }, {t -> t.printStackTrace() })
    }

    fun unsubscribeLocation() {
        locationSubscription.dispose()
        locationSubscription = CompositeDisposable()
    }

    interface AroundMeView : FragmentView {
        fun renderLocation(location: Location)
        fun renderItemsAround(aroundItems: List<AroundEntity>)
        fun openGroupDetails(uid: String)
    }

    companion object {
        internal val OFFSET = 0.00002
    }

}
