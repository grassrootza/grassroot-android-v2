package za.org.grassroot2.view;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public interface LoginView extends GrassrootView {

    Observable<CharSequence> usernameChanged();
    Observable<CharSequence> otpChanged();

    void toggleNextButton(boolean enabled);
    void requestOtpEntry(String defaultValue);
    void displayInvalidUsername();
    void displayInvalidOtp();
    void loginSuccessContinue(@NonNull String authToken, @NonNull Class<?> nextActivity);
}
