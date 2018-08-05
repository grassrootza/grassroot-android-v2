package za.org.grassroot2.presenter.activity

import retrofit2.Response
import za.org.grassroot2.model.exception.AuthenticationInvalidException
import za.org.grassroot2.model.exception.ServerUnreachableException

interface GrassrootPresenter {
    fun handleResponseError(response: Response<*>)
    fun handleNetworkConnectionError(t: Throwable)
    fun handleNetworkUploadError(t: Throwable)
    fun handleAuthenticationError(t: AuthenticationInvalidException)
    fun handleServerUnreachableError(t: ServerUnreachableException)

}
