package za.org.grassroot.android.view;

import android.app.Activity;

import java.io.File;

public interface PictureView {
    interface view{
        void showToastMessage(String msg);
    }
    interface presenter{
        File onCameraBtnClick(Activity activity, String filePath);
        void onGalleryBtnClick(Activity activity);
    }
}
