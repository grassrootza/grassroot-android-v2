package za.org.grassroot.android.view;

import io.reactivex.Observable;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;

/**
 * Created by luke on 2017/07/06.
 */

public interface GrassrootView {

    // emits true when connection restored, or false if
    Observable<ConnectionResult> showConnectionFailedDialog();
    Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog();
}
