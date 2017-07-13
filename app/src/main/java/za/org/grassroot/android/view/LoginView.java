package za.org.grassroot.android.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/07/06.
 */

public interface LoginView extends GrassrootView {

    Observable<CharSequence> usernameChanged();
    Observable<CharSequence> usernameNext();
    Observable<CharSequence> otpEntered();

    void toggleNextButton(boolean enabled);
    void requestOtpEntry();
    void displayInvalidUsername();
    void displayInvalidOtp();
}