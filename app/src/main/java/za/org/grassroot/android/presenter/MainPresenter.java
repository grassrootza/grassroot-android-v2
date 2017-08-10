package za.org.grassroot.android.presenter;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.MainView;

public class MainPresenter extends LoggedInViewPresenter implements MainView.presenter {

    private MainView.view view;

    @Inject
    public MainPresenter(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    public void attachMainView(MainView.view view) {
        this.view = view;
    }


    @Override
    protected void onViewAttached() {

    }

    @Override
    public void onLogoutBtnClick() {
        logoutRetainingData();
    }

    @Override
    public void onVideoBtnClick() {
        view.gotoVideoActivity();
    }

    @Override
    public void onAudioBtnClick() {
        view.gotoAudioActivity();
    }

    @Override
    public void onPictureBtnClick() {
        view.gotoPictureActivity();

    }

    @Override
    public void permissionStatus(Boolean storagePermission, Boolean recordPermission) {
        if (storagePermission && recordPermission) {
            view.showToastMessage("Permission Granted");
        }
        else
        {
            view.showToastMessage("Permission Denied");
        }
    }

    @Override
    public void cleanUpForActivity() {
        Timber.e("Need to fix up in here");
    }
}
