package za.org.grassroot.android.services.rest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;
import za.org.grassroot.android.services.auth.GrassrootAuthUtils;

public final class AddTokenInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();
        final String token = GrassrootAuthUtils.getToken();
        if (token != null) {
            Timber.d("Adding header: " + token);
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        requestBuilder.addHeader("Accept", "application/json");
        return chain.proceed(requestBuilder.build());
    }
}
