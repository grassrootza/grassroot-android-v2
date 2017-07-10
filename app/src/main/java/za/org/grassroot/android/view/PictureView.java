package za.org.grassroot.android.view;

import android.app.Activity;

import java.io.File;

/**
 * Created by Pial on 10-Jul-17.
 */

public interface PictureView {
    interface view{
        void showToastMessage(String msg);
    }
    interface presenter{
        File onCameraBtnClick(Activity activity, String filePath);
        void onGalleryBtnClick(Activity activity);


    }
}
