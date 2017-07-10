package za.org.grassroot.android.view;


import android.app.Activity;

/**
 * Created by Pial on 10-Jul-17.
 */

public interface VideoActivityView {
    interface view{
        void showToastMessage(String msg);
    }
    interface presenter{
        void onTakeVideoBtn(Activity activity, String filePath);


    }
}
