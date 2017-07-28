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

import timber.log.Timber;
import za.org.grassroot.android.presenter.LoginPresenter;
import za.org.grassroot.android.view.LoginActivity;

/**
 * Created by luke on 2017/07/26.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context context;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
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
        // intent.putExtra(LoginPresenter.PARAM_AUTHTOKEN_TYPE, authTokenType);
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
        final String storedJwt = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(storedJwt)) {
            // try get a refreshed token ...
            Timber.i("no JWT found in account manager");
        }

        final Bundle result = new Bundle();
        if (!TextUtils.isEmpty(storedJwt)) {
            Timber.i("have a JWT, use it");
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, storedJwt);
        } else {
            Timber.i("no JWT, so go to login");
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
