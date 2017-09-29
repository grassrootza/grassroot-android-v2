package za.org.grassroot2.services.rest;

import android.accounts.Account;
import android.accounts.AccountManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;
import za.org.grassroot2.services.account.AuthConstants;

public final class AddTokenInterceptor implements Interceptor {

    private AccountManager accountManager;

    private static final int HTTP_UNAUTHORIZED_CODE = 401;

    @Inject
    public AddTokenInterceptor(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();
        final String token = getToken();
        if (token != null) {
            Timber.v("Adding header: " + token);
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        requestBuilder.addHeader("Accept", "application/json");
        Response response = chain.proceed(requestBuilder.build());
        invalidateTokenIfExpired(response);
        return response;
    }

    private void invalidateTokenIfExpired(Response response) {
        if (response.code() == HTTP_UNAUTHORIZED_CODE) {
            Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
            if (accounts.length != 0 ) {
                accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE,
                        accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE));
            }
        }
    }

    private String getToken() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE);
    }
}
