package za.org.grassroot.android.view;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.services.auth.AuthConstants;
import za.org.grassroot.android.view.activity.GrassrootActivity;
import za.org.grassroot.android.view.fragment.SingleTextInputFragment;

/**
 * Created by luke on 2017/07/07.
 */

public class LoginActivity extends GrassrootActivity implements LoginView {

    private LoginPresenter loginPresenter;

    private SingleTextInputFragment currentFragment; // just as a pointer
    private SingleTextInputFragment usernameFragment;
    private SingleTextInputFragment otpFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (loginPresenter == null) {
            loginPresenter = new LoginPresenter();
        }

        usernameFragment = SingleTextInputFragment.newInstance(R.string.login_welcome,
                R.string.login_enter_msisdn,
                R.string.login_button_register,
                R.string.button_next);
        usernameFragment.viewCreated().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence sequence) throws Exception {
                loginPresenter.attach(LoginActivity.this);
                usernameFragment.toggleNextDoneButton(false);
            }
        });

        otpFragment = SingleTextInputFragment.newInstance(R.string.login_enter_otp_banner,
                R.string.login_enter_otp_string,
                R.string.login_button_register,
                R.string.button_login);
        otpFragment.viewCreated().subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(@NonNull CharSequence sequence) throws Exception {
                loginPresenter.attachOtp(LoginActivity.this);
                otpFragment.toggleNextDoneButton(false);
                otpFragment.toggleBackOtherButton(false);
            }
        });

        currentFragment = usernameFragment;
        setToUsernameEntry();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loginPresenter.detach(this);
    }

    private void setToUsernameEntry() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.login_frag_holder, usernameFragment, "USERNAME")
                .commit();
        currentFragment = usernameFragment;
    }

    @Override
    public void requestOtpEntry(String defaultValue) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.login_frag_holder, otpFragment, "OTP")
                .addToBackStack("OTP")
                .commit();
        currentFragment = otpFragment;
    }

    @Override
    public Observable<CharSequence> usernameChanged() {
        return usernameFragment.textInputChanged();
    }

    @Override
    public Observable<CharSequence> usernameNext() {
        return usernameFragment.textInputNextDone();
    }

    @Override
    public Observable<CharSequence> otpChanged() {
        return otpFragment.textInputChanged();
    }

    @Override
    public Observable<CharSequence> otpEntered() {
        return otpFragment.textInputNextDone();
    }

    @Override
    public Observable<CharSequence> newUserClicked() {
        return otpFragment.textInputBackOther();
    }

    @Override
    public void toggleNextButton(boolean enabled) {
        currentFragment.toggleNextDoneButton(enabled);
    }

    @Override
    public void displayInvalidUsername() {

    }

    @Override
    public void displayInvalidOtp() {

    }

    @Override
    public void loginSuccessContinue(@NonNull String authToken, @NonNull Class<?> nextActivity) {
        Intent intent = new Intent(this, nextActivity);
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, AuthConstants.ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AuthConstants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(Activity.RESULT_OK, intent);
        finish();
        startActivity(intent);
    }

    @Override
    public Observable<ConnectionResult> showConnectionFailedDialog() {
        return null;
    }

    @Override
    public Observable<AuthRecoveryResult> showAuthenticationRecoveryDialog() {
        return null;
    }

    @Override
    public void showProgressBar() {
        currentFragment.showProgressBar();
    }

    @Override
    public void closeProgressBar() {
        currentFragment.closeProgressBar();
    }
}
