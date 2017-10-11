package za.org.grassroot2.services.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.rest.GrassrootAuthApi;
import za.org.grassroot2.view.LoginActivity;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context context;

    @Inject GrassrootAuthApi   authApi;
    @Inject UserDetailsService userService;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        ((GrassrootApplication)context.getApplicationContext()).getAppComponent().inject(this);
        Timber.e("created the account authenticator");
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }



    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType,
                             String authTokenType, String[] features, Bundle options) throws NetworkErrorException {
        Timber.e("adding an account! inside authenticator, of type: " + accountType);
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        intent.putExtra(LoginActivity.EXTRA_NEW_ACCOUNT, true);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        final AccountManager am = AccountManager.get(context);
        String authToken = am.peekAuthToken(account, authTokenType);
        final Bundle result = new Bundle();
        if (!TextUtils.isEmpty(authToken)) {
            Timber.d("have a JWT, use it");
            addTokenToResultBundle(account, authToken, result);
        } else {
            Timber.d("no JWT, so go to login");
            tryTokenRefresh(accountAuthenticatorResponse, account, am, result);
        }
        return result;
    }

    private void tryTokenRefresh(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, AccountManager am, Bundle result) {
        String oldToken = am.getUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN);
        authApi.refreshOtp(oldToken, null).subscribe(response -> {
            if (response.isSuccessful()) {
                addTokenToResultBundle(account, response.body().getData(), result);
            } else {
                userService.logout(false, false).subscribe(aBoolean -> {}, Timber::d);
                final Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
                intent.putExtra(LoginActivity.EXTRA_TOKEN_EXPIRED, true);
                result.putParcelable(AccountManager.KEY_INTENT, intent);
            }
        }, Timber::e);
    }

    private void addTokenToResultBundle(Account account, String authToken, Bundle result) {
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return AuthConstants.AUTH_TOKENTYPE.equals(authTokenType) ? authTokenType : null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }


}
