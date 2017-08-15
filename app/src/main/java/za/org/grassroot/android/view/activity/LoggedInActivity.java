package za.org.grassroot.android.view.activity;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;

import timber.log.Timber;
import za.org.grassroot.android.presenter.LoggedInViewPresenter;
import za.org.grassroot.android.view.LoggedInView;

/**
 * Created by luke on 2017/08/10.
 */

public abstract class LoggedInActivity extends GrassrootActivity implements LoggedInView {

    protected static final int REQUEST_PERMISSION_CODE = 901;

    protected LoggedInViewPresenter activePresenter;

    // later see if Dagger can do this for us (for now, use of abstract class makes this not work)
    protected void setActivePresenter(LoggedInViewPresenter presenter) {
        this.activePresenter = presenter;
    }

    @Override
    public void requestPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void launchActivityForResult(Intent intent, int requestCode) {
        if (activePresenter == null) {
            Timber.e("ERROR! Trying to start activity for result with nowhere to send result back to");
        } else {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (activePresenter != null) {
            if (resultCode == RESULT_OK) {
                activePresenter.handleActivityResult(requestCode, data);
            } else {
                activePresenter.handleActivityResultError(requestCode, resultCode, data);
            }
        } else {
            Timber.d("onActivityResult called without an insantiated presenter");
        }
    }

}
