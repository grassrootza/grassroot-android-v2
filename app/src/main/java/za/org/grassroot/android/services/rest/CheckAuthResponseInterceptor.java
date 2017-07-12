package za.org.grassroot.android.services.rest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import za.org.grassroot.android.model.exception.AuthenticationInvalidException;

/**
 * Created by luke on 2017/07/12.
 */

public class CheckAuthResponseInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        // check for invalid auth responses
        if (false) {
            throw new AuthenticationInvalidException();
        } else {
            return originalResponse;
        }
    }
}
