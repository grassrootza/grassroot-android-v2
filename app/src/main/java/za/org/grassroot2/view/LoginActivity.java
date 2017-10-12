package za.org.grassroot2.view;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.model.enums.AuthRecoveryResult;
import za.org.grassroot2.presenter.LoginPresenter;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.activity.MainActivity;
import za.org.grassroot2.view.fragment.SingleTextInputFragment;

public class LoginActivity extends GrassrootActivity implements LoginView {

    public static final String EXTRA_NEW_ACCOUNT   = "extra_new_account";
    public static final String EXTRA_TOKEN_EXPIRED = "extra_token_expired";
    @Inject LoginPresenter loginPresenter;

    private SingleTextInputFragment currentFragment; // just as a pointer
    private SingleTextInputFragment usernameFragment;
    private SingleTextInputFragment otpFragment;

    private String         debugOtp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ((GrassrootApplication) getApplication())
                .getAppComponent().plus(getActivityModule()).inject(this);
        loginPresenter.attach(LoginActivity.this);
        if (loggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        usernameFragment = SingleTextInputFragment.newInstance(R.string.login_welcome,
                R.string.login_enter_msisdn,
                R.string.login_button_register,
                R.string.button_next);
        disposables.add(usernameFragment.viewCreated().subscribe(integer -> {
            loginPresenter.onViewCreated();
            usernameFragment.toggleNextDoneButton(false);
            usernameFragment.setInputType(InputType.TYPE_CLASS_PHONE);
            usernameFragment.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            usernameFragment.focusOnInput();
        }, throwable -> {}));

        otpFragment = SingleTextInputFragment.newInstance(R.string.login_enter_otp_banner,
                R.string.login_enter_otp_string,
                R.string.login_button_register,
                R.string.button_login);
        disposables.add(otpFragment.viewCreated().subscribe(integer -> {
            otpFragment.toggleNextDoneButton(false);
            otpFragment.toggleBackOtherButton(false);
            if (!TextUtils.isEmpty(debugOtp)) {
                otpFragment.setInputDefault(debugOtp);
            }
            usernameFragment.setInputType(InputType.TYPE_CLASS_NUMBER);
            usernameFragment.setImeOptions(EditorInfo.IME_ACTION_DONE);
            usernameFragment.focusOnInput();
        },  throwable -> {}));

        currentFragment = usernameFragment;
        setToUsernameEntry();
        if (getIntent().hasExtra(EXTRA_TOKEN_EXPIRED)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.token_expired, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loginPresenter.detach();
    }

    private void setToUsernameEntry() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.login_frag_holder, usernameFragment, "USERNAME")
                .commit();
        currentFragment = usernameFragment;
    }

    @Override
    public void requestOtpEntry(String defaultValue) {
        if (BuildConfig.DEBUG) {
            debugOtp = defaultValue;
        }
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
    public Observable<CharSequence> otpChanged() {
        return otpFragment.textInputChanged();
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
    public void showNoConnectionMessage() {

    }

    @Subscribe
    public void singleInput(SingleTextInputFragment.SingleInputTextEvent e) {
        loginPresenter.processInput(e.value);
    }

}
