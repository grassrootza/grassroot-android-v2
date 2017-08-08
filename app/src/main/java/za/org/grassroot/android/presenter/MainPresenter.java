package za.org.grassroot.android.presenter;

import za.org.grassroot.android.view.MainView;

public class MainPresenter implements MainView.presenter {

    private final MainView.view view;
    public MainPresenter(MainView.view view)
    {
        this.view=view;
    }
    public void makeToast(String meg)
    {
        view.showToastMessage(meg);
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
        if (storagePermission && recordPermission)
        {
            view.showToastMessage("Permission Granted");
        }
        else
        {
            view.showToastMessage("Permission Denied");
        }
    }
}
