package za.org.grassroot2.services;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private final FragmentActivity context;
    private final GoogleApiClient  googleApiClient;
    private       LocationRequest  oneTimeLocationRequest;
    private PublishSubject<Location> locationSubject = PublishSubject.create();

    @Inject
    public LocationManager(FragmentActivity c) {
        context = c;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(c, this)
                .build();
        createOneTimeLocationRequest();
    }

    public Observable<Location> getCurrentLocation() {
        return locationSubject;
    }

    public void disconnect() {
        googleApiClient.stopAutoManage(context);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(oneTimeLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());
        result.setResultCallback(locationSettingsResult -> {
            final com.google.android.gms.common.api.Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, oneTimeLocationRequest, LocationManager.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createOneTimeLocationRequest() {
        oneTimeLocationRequest = new LocationRequest();
        oneTimeLocationRequest.setInterval(10000);
        oneTimeLocationRequest.setFastestInterval(5000);
        oneTimeLocationRequest.setNumUpdates(1);
        oneTimeLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        locationSubject.onNext(location);
    }
}
