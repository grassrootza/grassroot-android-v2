package za.org.grassroot2.view;

import android.content.Intent;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/08/10.
 */

public interface LoggedInView extends GrassrootView {

    void requestPermission(String[] permissions);
    void launchActivityForResult(Intent intent, int requestCode);

    Observable<Boolean> logoutClicked();
    Observable<Boolean> syncTriggered();

}