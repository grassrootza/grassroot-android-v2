package za.org.grassroot.android.view;

import android.os.Bundle;

import io.reactivex.Observable;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;

public interface GrassrootView extends ProgressBarContainer {

    // emits true when connection restored, or false if
    Observable<ConnectionResult> showConnectionFailedDialog();
    Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog();

    void showSuccessMsg(int successMsg);
    void showSuccessMsg(String message);
    void showErrorToast(int errorTextRes);

    void closeKeyboard();

    void launchActivity(Class<?> cls, Bundle args);
}