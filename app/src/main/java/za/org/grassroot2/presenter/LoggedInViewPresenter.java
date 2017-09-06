package za.org.grassroot2.presenter;

import android.content.Intent;

/**
 * Created by luke on 2017/08/11.
 */

public interface LoggedInViewPresenter extends ViewPresenter {

    void handleActivityResult(int requestCode, Intent data);
    void handleActivityResultError(int requestCode, int resultCode, Intent data);

    void logoutRetainingData();
    void logoutWipingData();
    void triggerAccountSync();

}
