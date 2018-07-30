package za.org.grassroot2.services.rest;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;
import za.org.grassroot2.services.OfflineReceiver;
import za.org.grassroot2.services.account.AuthConstants;
import za.org.grassroot2.util.AlarmManagerHelper;
import za.org.grassroot2.util.UserPreference;

public final class AddTokenInterceptor implements Interceptor {

    public static final Set<String> UPLOAD_METHODS = new HashSet<>(Arrays.asList("POST", "PUT", "PATCH"));
    private UserPreference userPreference;
    private AccountManager accountManager;

    private static final int HTTP_UNAUTHORIZED_CODE = 401;
    private Context context;

    @Inject
    public AddTokenInterceptor(Context c, AccountManager accountManager, UserPreference userPreference) {
        context = c;
        this.accountManager = accountManager;
        this.userPreference = userPreference;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        boolean isUpload = UPLOAD_METHODS.contains(original.method());
        Request.Builder requestBuilder = original.newBuilder();
        final String token = getToken();
        if (token != null) {
            Timber.v("Adding header: %s", token);
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        } else {
            EventBus.getDefault().postSticky(new TokenRefreshEvent());
        }
        requestBuilder.addHeader("Accept", "application/json");
        Response response = chain.proceed(requestBuilder.build());
        checkResponse(response, isUpload);
        return response;
    }

    private void checkResponse(Response response, boolean isUpload) {
        if (response.code() == HTTP_UNAUTHORIZED_CODE) {
            invalidateToken();
        } else if (response.isSuccessful()) {
            userPreference.setNoConnectionInfoDisplayed(false);
            AlarmManagerHelper.INSTANCE.cancelAlarmForBroadcastReceiver(context, OfflineReceiver.class);
        }
    }

    private void invalidateToken() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE,
                    accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE));
            EventBus.getDefault().postSticky(new TokenRefreshEvent());
        }
    }

    private String getToken() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accountManager.peekAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE);
    }

    public static class TokenRefreshEvent {
    }
}
