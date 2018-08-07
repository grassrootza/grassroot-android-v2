package za.org.grassroot2.services

import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class LocationManager @Inject
constructor(a: AppCompatActivity) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val context: FragmentActivity
    private val googleApiClient: GoogleApiClient
    private var oneTimeLocationRequest: LocationRequest? = null
    private val locationSubject = PublishSubject.create<Location>()

    val currentLocation: Observable<Location>
        get() {
            if (googleApiClient.isConnected) {
                checkSettingsAndReuqestLocation()
            } else if (!googleApiClient.isConnected) {
                googleApiClient.connect()
            }
            return locationSubject
        }

    init {
        context = a
        googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        createOneTimeLocationRequest()
    }

    fun disconnect() {
        if (googleApiClient.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            googleApiClient.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        checkSettingsAndReuqestLocation()
    }

    private fun checkSettingsAndReuqestLocation() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(oneTimeLocationRequest)
        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                builder.build())
        result.setResultCallback { locationSettingsResult ->
            val status = locationSettingsResult.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, oneTimeLocationRequest, this@LocationManager)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    private fun createOneTimeLocationRequest() {
        oneTimeLocationRequest = LocationRequest()
        oneTimeLocationRequest!!.interval = 10000
        oneTimeLocationRequest!!.fastestInterval = 5000
        oneTimeLocationRequest!!.numUpdates = 1
        oneTimeLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onLocationChanged(location: Location) {
        locationSubject.onNext(location)
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1
    }
}
