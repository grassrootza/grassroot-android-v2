package za.org.grassroot2.view.fragment

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Lazy
import kotlinx.android.synthetic.main.fragment_around_me.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.fragment.AroundMePresenter
import za.org.grassroot2.util.ImageUtil
import za.org.grassroot2.view.activity.GroupDetailsActivity
import javax.inject.Inject

class AroundMeFragment : GrassrootFragment(), AroundMePresenter.AroundMeView, GoogleMap.InfoWindowAdapter {

    @Inject internal lateinit var rxPermissions: Lazy<RxPermissions>
    @Inject internal lateinit var presenter: AroundMePresenter
    @Inject internal lateinit var imageUtil: ImageUtil

    private var mapFragment: SupportMapFragment? = null
    private lateinit var googleMap: GoogleMap

    override fun onInject(activityComponent: ActivityComponent) {
        get().inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_around_me
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
        initToolbar()
        mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction().add(R.id.mapContainer, mapFragment).commit()
        mapFragment!!.getMapAsync { googleMap ->
            setMarkerInfoClick(googleMap)
            requestLocation()
        }

    }

    private fun setMarkerInfoClick(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setInfoWindowAdapter(this)
        googleMap.setOnInfoWindowClickListener { marker ->
            val item = marker.tag as AroundEntity
            if (item.type == GrassrootEntityType.GROUP) {
                item.uid.let {
                    presenter.openGroupDetail(item.uid!!)
                }
            }
        }
    }

    private fun initToolbar() {
        setHasOptionsMenu(false)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.title_around)
    }

    private fun requestLocation() {
        disposables.add(rxPermissions.get().request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe({ aBoolean ->
            if (aBoolean) {
                presenter.getCurrentLocation()
            } else {
                throw Exception("Location permission not granted!")
            }
        }, {t -> t.printStackTrace()}))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun renderLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f), 1000, null)
        presenter.loadItemsAround()
    }

    override fun renderItemsAround(aroundItems: List<AroundEntity>) {
        googleMap.clear()
        aroundItems.forEach { item ->
            when {
                item.type == GrassrootEntityType.GROUP -> renderGroupMarker(item)
                item.type == GrassrootEntityType.MEETING -> renderMeetingMarker(item)
                item.type == GrassrootEntityType.LIVE_WIRE_ALERT -> renderLivewireMarker(item)
            }
        }
    }

    override fun openGroupDetails(uid: String) {
        GroupDetailsActivity.start(activity, uid)
    }

    private fun renderLivewireMarker(item: AroundEntity) {
        val latLng = LatLng(item.latitude!!, item.longitude!!)
        val m = googleMap.addMarker(MarkerOptions().position(latLng).title(item.title).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_alert_marker))))
        m.tag = item
    }

    private fun renderMeetingMarker(item: AroundEntity) {
        val latLng = LatLng(item.latitude!!, item.longitude!!)
        val m = googleMap.addMarker(MarkerOptions().position(latLng).title(item.title).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_meeting_marker))))
        m.tag = item
    }

    private fun renderGroupMarker(item: AroundEntity) {
        val latLng = LatLng(item.latitude!!, item.longitude!!)
        val m = googleMap.addMarker(MarkerOptions().position(latLng).title(item.title).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_around_marker))))
        m.tag = item
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        val item = marker.tag as AroundEntity
        when (item.type) {
            GrassrootEntityType.GROUP -> {
                val v = LayoutInflater.from(activity).inflate(R.layout.marker_group, null, false)
                (v.findViewById(R.id.title) as TextView).text = item.title
                return v
            }
            GrassrootEntityType.MEETING -> {
                val v = LayoutInflater.from(activity).inflate(R.layout.marker_meeting, null, false)
                (v.findViewById(R.id.title) as TextView).text = item.title
                (v.findViewById(R.id.description) as TextView).text = item.description
                return v
            }
            GrassrootEntityType.LIVE_WIRE_ALERT -> {
                val v = LayoutInflater.from(activity).inflate(R.layout.marker_alert, null, false)
                (v.findViewById(R.id.name) as TextView).text = item.title
                return v
            }
            else -> return null
        }
    }

    companion object {
        fun newInstance(): Fragment {
            return AroundMeFragment()
        }
    }

}
