package za.org.grassroot.android.services.rest;

import java.io.IOException;
import java.net.ConnectException;

import okhttp3.Interceptor;
import okhttp3.Response;
import za.org.grassroot.android.model.exception.AuthenticationInvalidException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.exception.ServerUnreachableException;

/**
 * Created by luke on 2017/07/12.
 */

public class CommonErrorHandlerInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            Response originalResponse = chain.proceed(chain.request());
            if (false) {
                throw new AuthenticationInvalidException();
            } else {
                return originalResponse;
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new ServerUnreachableException();
        }
        // check for invalid auth responses
    }
}