package za.org.grassroot2.view.fragment;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.Lazy;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.activity.ActivityComponent;
import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.presenter.AroundMePresenter;
import za.org.grassroot2.util.ImageUtil;
import za.org.grassroot2.view.activity.GroupDetailsActivity;

public class AroundMeFragment extends GrassrootFragment implements AroundMePresenter.AroundMeView, GoogleMap.InfoWindowAdapter {

    @BindView(R.id.mapContainer) FrameLayout mapContainer;
    @BindView(R.id.toolbar)      Toolbar     toolbar;

    @Inject Lazy<RxPermissions> rxPermissions;
    @Inject AroundMePresenter   presenter;
    @Inject ImageUtil           imageUtil;

    private SupportMapFragment mapFragment;
    private GoogleMap          googleMap;

    @Override
    protected void onInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_around_me;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attach(this);
        initToolbar();
        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.mapContainer, mapFragment).commit();
        mapFragment.getMapAsync(googleMap -> {
            setMarkerInfoClick(googleMap);
            requestLocation();
        });

    }

    private void setMarkerInfoClick(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setInfoWindowAdapter(this);
        googleMap.setOnInfoWindowClickListener(marker -> {
            AroundItem item = (AroundItem) marker.getTag();
            if (item instanceof Group) {
                presenter.openGroupDetail(((Group) item).getUid());
            }
        });
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_around);
    }

    private void requestLocation() {
        disposables.add(rxPermissions.get().request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe(aBoolean -> {
            if (aBoolean) {
                presenter.getCurrentLocation();
            } else {
                throw new Exception("Location permission not granted!");
            }
        }, Throwable::printStackTrace));
    }


    public static Fragment newInstance() {
        return new AroundMeFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detach(this);
    }

    @Override
    public void renderLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10), 1000, null);
        presenter.loadItemsAround();
    }

    @Override
    public void renderItemsAround(List<AroundItem> liveWireAlerts) {
        for (AroundItem item : liveWireAlerts) {
            if (item instanceof Group) {
                renderGroupMarker((Group) item);
            } else if (item instanceof Meeting) {
                renderMeetingMarker((Meeting) item);
            } else if (item instanceof LiveWireAlert) {
                renderLivewireMarker((LiveWireAlert) item);
            }
        }
    }

    @Override
    public void openGroupDetails(String uid) {
        GroupDetailsActivity.start(getActivity(), uid);
    }

    private void renderLivewireMarker(LiveWireAlert item) {
        LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
        Marker m = googleMap.addMarker(new MarkerOptions().position(latLng).title(item.getHeadline()).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_alert_marker))));
        m.setTag(item);
    }

    private void renderMeetingMarker(Meeting item) {
        LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
        Marker m = googleMap.addMarker(new MarkerOptions().position(latLng).title(item.getName()).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_meeting_marker))));
        m.setTag(item);
    }

    private void renderGroupMarker(Group item) {
        LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
        Marker m = googleMap.addMarker(new MarkerOptions().position(latLng).title(item.getName()).icon(BitmapDescriptorFactory.fromBitmap(imageUtil.drawableToBitmap(R.drawable.ic_around_marker))));
        m.setTag(item);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        AroundItem item = (AroundItem) marker.getTag();
        if (item instanceof Group) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.marker_group, null, false);
            ((TextView) v.findViewById(R.id.title)).setText(((Group) item).getName());
            return v;
        } else if (item instanceof Meeting) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.marker_meeting, null, false);
            ((TextView) v.findViewById(R.id.title)).setText(((Meeting) item).getName());
            ((TextView) v.findViewById(R.id.description)).setText(((Meeting) item).getDescription());
            return v;
        } else if (item instanceof LiveWireAlert) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.marker_alert, null, false);
            ((TextView) v.findViewById(R.id.name)).setText(((LiveWireAlert) item).getHeadline());
            return v;
        }
        return null;
    }

}
