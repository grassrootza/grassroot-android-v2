package za.org.grassroot.android.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/07/06.
 */

public interface LoginView extends GrassrootView {

    Observable<CharSequence> usernameEntered();
    Observable<CharSequence> otpEntered();

    void requestOtpEntry();
    void gotoActivity();
}