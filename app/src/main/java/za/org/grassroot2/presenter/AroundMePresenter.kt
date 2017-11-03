package za.org.grassroot2.presenter

import android.location.Location

import java.util.HashSet

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter
import za.org.grassroot2.services.LocationManager
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.FragmentView


class AroundMePresenter @Inject
constructor(private val locationManager: LocationManager, private val networkService: NetworkService, private val dbService: DatabaseService) : BaseFragmentPresenter<AroundMePresenter.AroundMeView>() {
    private val testLat = -26.1925350
    private val testLong = 28.0373235
    private val existingLocations = HashSet<String>()

    override fun detach(view: AroundMeView) {
        super.detach(view)
        locationManager.disconnect()
    }

    fun getCurrentLocation() {
        val l = Location("")
        l.latitude = testLat
        l.longitude = testLong
        locationManager.currentLocation.subscribe({ location -> view.renderLocation(l) }, {t -> t.printStackTrace() })
    }

    override fun onViewCreated() {}

    fun loadItemsAround() {
        networkService.getAllAround(testLat, testLong, 10000).flatMap { aroundItems ->
            adjustLocations(aroundItems)
            Observable.just(aroundItems)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ itemsAround -> view.renderItemsAround(itemsAround) }, {t -> t.printStackTrace() })
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

    interface AroundMeView : FragmentView {
        fun renderLocation(location: Location)
        fun renderItemsAround(aroundItems: List<AroundEntity>)
        fun openGroupDetails(uid: String)
    }

    companion object {
        internal val OFFSET = 0.00002
    }

}
