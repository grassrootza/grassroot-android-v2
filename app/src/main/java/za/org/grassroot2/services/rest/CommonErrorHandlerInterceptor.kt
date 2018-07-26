package za.org.grassroot2.services.rest

import java.io.IOException
import java.net.ConnectException

import okhttp3.Interceptor
import okhttp3.Response
import za.org.grassroot2.model.exception.AuthenticationInvalidException
import za.org.grassroot2.model.exception.ServerUnreachableException

class CommonErrorHandlerInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: ConnectException) {
            e.printStackTrace()
            throw ServerUnreachableException()
        }

        // check for invalid auth responses
    }
}
