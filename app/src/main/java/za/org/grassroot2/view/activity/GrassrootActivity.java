package za.org.grassroot2.view.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.Lazy;
import io.reactivex.Observable;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.activity.ActivityModule;
import za.org.grassroot2.model.enums.AuthRecoveryResult;
import za.org.grassroot2.model.enums.ConnectionResult;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.services.rest.AddTokenInterceptor;
import za.org.grassroot2.view.GrassrootView;

public abstract class GrassrootActivity extends AppCompatActivity implements GrassrootView {

    @Inject public Lazy<AccountManager> accountManagerProvider;
    public         AccountManager       accountManager;

    private AccountAuthenticatorResponse authResponse     = null;
    private Bundle                       authResultBundle = null;

    @BindView(R.id.progressBar)
    @Nullable
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ((GrassrootApplication) getApplication()).getAppComponent().plus(getActivityModule()).inject(this);
        authResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authResponse != null) {
            authResponse.onRequestContinued();
        }
    }

    protected boolean loggedIn() {
        if (accountManager == null) {
            accountManager = accountManagerProvider.get();
        }
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length != 0 && !TextUtils.isEmpty(accountManager.getUserData(accounts[0], AuthConstants.USER_DATA_LOGGED_IN));
    }

    @Override
    public Activity getActivity() {
        return this;
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
    public void showSuccessMsg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessMsg(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorToast(int errorTextRes) {
        Toast.makeText(this, errorTextRes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void launchActivity(Class<?> cls, Bundle args) {
        Intent i = new Intent(this, cls);
        if (args != null) {
            i.putExtras(args);
        }
        startActivity(i);
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        authResultBundle = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    @Override
    public void finish() {
        if (authResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (authResultBundle != null) {
                authResponse.onResult(authResultBundle);
            } else {
                authResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            authResponse = null;
        }
        super.finish();
    }

    @Override
    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showProgressBar() {
        safeToggleProgressBar(progressBar, true);
    }

    public void closeProgressBar() {
        safeToggleProgressBar(progressBar, false);
    }

    protected void safeToggleProgressBar(ProgressBar progressBar, boolean shown) {
        if (progressBar != null) {
            progressBar.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

    public ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    public AppComponent getAppComponent() {
        return ((GrassrootApplication) getApplication()).getAppComponent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    void tokenRefreshEvent(AddTokenInterceptor.TokenRefreshEvent e) {
        EventBus.getDefault().removeStickyEvent(e);
        accountManager = accountManagerProvider.get();
        final Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        accountManager.getAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, null, this, future -> {
            try {
                if (future.getResult().containsKey(AccountManager.KEY_AUTHTOKEN)) {
                    accountManager.setAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                    accountManager.setUserData(accounts[0], AuthConstants.USER_DATA_CURRENT_TOKEN, future.getResult().getString(AccountManager.KEY_AUTHTOKEN));
                }
            } catch (OperationCanceledException | IOException | AuthenticatorException e1) {
                e1.printStackTrace();
            }
        }, null);
    }

}
