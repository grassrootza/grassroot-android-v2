package za.org.grassroot2.view;

import android.app.Activity;
import android.os.Bundle;

import io.reactivex.Observable;
import za.org.grassroot2.model.enums.AuthRecoveryResult;
import za.org.grassroot2.model.enums.ConnectionResult;

public interface GrassrootView extends ProgressBarContainer {

    Activity getActivity();

    Observable<ConnectionResult> showConnectionFailedDialog();
    Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog();

    void showSuccessMsg(int successMsg);
    void showSuccessMsg(String message);
    void showErrorToast(int errorTextRes);

    void closeKeyboard();

    void launchActivity(Class<?> cls, Bundle args);
    void cleanUpActivity();
}