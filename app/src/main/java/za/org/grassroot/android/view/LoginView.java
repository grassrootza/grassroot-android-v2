package za.org.grassroot.android.view;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

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
