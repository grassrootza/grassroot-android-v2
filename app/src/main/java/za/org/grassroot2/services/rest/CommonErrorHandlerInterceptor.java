package za.org.grassroot2.services.rest;

import java.io.IOException;
import java.net.ConnectException;

import okhttp3.Interceptor;
import okhttp3.Response;
import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.ServerUnreachableException;

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
