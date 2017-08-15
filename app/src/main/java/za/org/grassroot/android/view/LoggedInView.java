package za.org.grassroot.android.view;

import android.content.Intent;

/**
 * Created by luke on 2017/08/10.
 */

public interface LoggedInView extends GrassrootView {

    void requestPermission(String[] permissions);
    void launchActivityForResult(Intent intent, int requestCode);

}