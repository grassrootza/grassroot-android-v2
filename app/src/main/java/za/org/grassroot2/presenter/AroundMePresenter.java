package za.org.grassroot2.presenter;

import android.location.Location;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.presenter.fragment.BaseFragmentPresenter;
import za.org.grassroot2.services.LocationManager;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.FragmentView;


public class AroundMePresenter extends BaseFragmentPresenter<AroundMePresenter.AroundMeView> {

    private LocationManager locationManager;
    private DatabaseService dbService;
    private NetworkService  networkService;
    private double      testLat           = -26.1925350;
    private double      testLong          = 28.0373235;
    static final double OFFSET = 0.00002;
    private Set<String> existingLocations = new HashSet<>();

    @Inject
    public AroundMePresenter(LocationManager manager, NetworkService networkService, DatabaseService dbService) {
        locationManager = manager;
        this.networkService = networkService;
        this.dbService = dbService;
    }

    @Override
    public void detach(AroundMeView view) {
        super.detach(view);
        locationManager.disconnect();
    }

    public void getCurrentLocation() {
        Location l = new Location("");
        l.setLatitude(testLat);
        l.setLongitude(testLong);
        locationManager.getCurrentLocation().subscribe(location -> view.renderLocation(l), Throwable::printStackTrace);
    }

    @Override
    public void onViewCreated() {
    }

    public void loadItemsAround() {
        networkService.getAllAround(testLat, testLong, 10000).flatMap(aroundItems -> {
            adjustLocations(aroundItems);
            return Observable.just(aroundItems);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(itemsAround -> view.renderItemsAround(itemsAround), Throwable::printStackTrace);
    }

    private void adjustLocations(List<AroundItem> aroundItems) {
        int i = 0;
        for (AroundItem item : aroundItems) {
            String location = getLocationAsString(item);
            if (existingLocations.contains(location)) {
                item.setLatitude(item.getLatitude() + i * OFFSET);
                item.setLongitude(item.getLongitude() + i * OFFSET);
                location = getLocationAsString(item);
                i++;
            }
            existingLocations.add(location);
        }
    }

    @NonNull
    private String getLocationAsString(AroundItem item) {
        return String.valueOf(item.getLatitude()) + String.valueOf(item.getLongitude());
    }

    public void openGroupDetail(String uid) {
        dbService.load(Group.class, uid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(group -> {
            view.openGroupDetails(group.getUid());
        }, Throwable::printStackTrace);
    }

    public interface AroundMeView extends FragmentView {
        void renderLocation(Location location);
        void renderItemsAround(List<AroundItem> liveWireAlerts);
        void openGroupDetails(String uid);
    }

}
