package za.org.grassroot.android.view.activity;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.Observable;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;
import za.org.grassroot.android.view.GrassrootView;

/**
 * Created by luke on 2017/07/07.
 */

public class GrassrootActivity extends AppCompatActivity implements GrassrootView {

    @Override
    public Observable<ConnectionResult> showConnectionFailedDialog() {
        return null;
    }

    @Override
    public Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog() {
        return null;
    }
}
