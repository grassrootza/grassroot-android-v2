package za.org.grassroot.android.view.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import javax.inject.Inject;

import io.reactivex.Observable;
import timber.log.Timber;
import za.org.grassroot.android.GrassrootApplication;
import za.org.grassroot.android.model.enums.AuthRecoveryResult;
import za.org.grassroot.android.model.enums.ConnectionResult;
import za.org.grassroot.android.services.auth.AuthConstants;
import za.org.grassroot.android.view.GrassrootView;
import za.org.grassroot.android.view.LoginActivity;

public abstract class GrassrootActivity extends AppCompatActivity implements GrassrootView {

    @Inject
    AccountManager accountManager;

    private AccountAuthenticatorResponse authResponse = null;
    private Bundle authResultBundle = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ((GrassrootApplication) getApplication())
                .getAppComponent()
                .inject(this);

        authResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (authResponse != null) {
            authResponse.onRequestContinued();
        }

        // consider moving to onResume later
        Timber.d("inside onCreate, checking if logged in");
        if (needsToLoginOrRegister()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    private boolean needsToLoginOrRegister() {
        if (this instanceof LoginActivity) {
            return false;
        } else {
            Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
            return accounts.length == 0 ||
                    TextUtils.isEmpty(accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE));
        }
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

}
