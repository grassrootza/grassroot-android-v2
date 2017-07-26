package za.org.grassroot.android.view;

import android.content.Intent;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

/**
 * Created by luke on 2017/07/06.
 */

public interface LoginView extends GrassrootView {

    Observable<CharSequence> usernameChanged();
    Observable<CharSequence> usernameNext();
    Observable<CharSequence> otpChanged();
    Observable<CharSequence> otpEntered();

    Observable<CharSequence> newUserClicked();

    void toggleNextButton(boolean enabled);
    void requestOtpEntry(String defaultValue);
    void displayInvalidUsername();
    void displayInvalidOtp();

    void loginSuccessContinue(@NonNull String authToken, @NonNull Class<?> nextActivity);
}