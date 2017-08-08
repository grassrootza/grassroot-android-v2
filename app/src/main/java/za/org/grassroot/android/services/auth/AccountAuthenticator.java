package za.org.grassroot.android.services.auth;

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
import za.org.grassroot.android.GrassrootApplication;
import za.org.grassroot.android.services.rest.GrassrootRestService;
import za.org.grassroot.android.services.user.UserDetailsService;
import za.org.grassroot.android.view.LoginActivity;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    @Inject
    GrassrootRestService grassrootRestService;

    private final Context context;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
        ((GrassrootApplication) context).getAppComponent().inject(this);
        Timber.i("created the account authenticator");
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType, String authTokenType, String[] features, Bundle options) throws NetworkErrorException {
        Timber.d("adding an account! inside authenticator, of type: " + accountType);
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
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

        // if JWT is empty, try authenticate
        if (TextUtils.isEmpty(authToken)) {
            // try get a refreshed token ...
            Timber.i("no JWT found in account manager");
            try {
                authToken = grassrootRestService.refreshOtp(UserDetailsService.getUserMsisdn(),
                        GrassrootAuthUtils.AUTH_CLIENT_TYPE).execute().body().getData().getToken();
            } catch (Exception e) {
                Timber.e(e, "Could not try to refresh token, passing along");

            }
        }

        final Bundle result = new Bundle();
        if (!TextUtils.isEmpty(authToken)) {
            Timber.d("have a JWT, use it");
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        } else {
            Timber.d("no JWT, so go to login");
            final Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
            result.putParcelable(AccountManager.KEY_INTENT, intent);
        }
        return result;
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
}
