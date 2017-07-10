package za.org.grassroot.android.view;

/**
 * Created by Pial on 09-Jul-17.
 */

public interface MainView {
    interface view{
        void gotoVideoActivity();
        void gotoAudioActivity();
        void gotoPictureActivity();
        void showToastMessage(String msg);
    }
    interface presenter{
        void onVideoBtnClick();
        void onAudioBtnClick();
        void onPictureBtnClick();
        void permissionStatus(Boolean storagePermission, Boolean recordPermission);
    }
}
