package za.org.grassroot.android.services.rest;

import android.accounts.Account;
import android.accounts.AccountManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;
import za.org.grassroot.android.services.auth.AuthConstants;

public final class AddTokenInterceptor implements Interceptor {

    private AccountManager accountManager;

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
        return chain.proceed(requestBuilder.build());
    }

    private String getToken() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE);
    }
}
