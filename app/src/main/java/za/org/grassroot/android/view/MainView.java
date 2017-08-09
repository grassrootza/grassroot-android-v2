package za.org.grassroot.android.view;

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
        void onLogoutBtnClick();
        void permissionStatus(Boolean storagePermission, Boolean recordPermission);
    }
}
