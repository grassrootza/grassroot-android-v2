package za.org.grassroot.android.view;


import android.app.Activity;

public interface VideoActivityView {
    interface view{
        void showToastMessage(String msg);
    }
    interface presenter{
        void onTakeVideoBtn(Activity activity, String filePath);

    }
}
