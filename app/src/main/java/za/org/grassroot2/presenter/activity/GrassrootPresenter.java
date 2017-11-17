package za.org.grassroot2.presenter.activity;

import retrofit2.Response;
import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.ServerUnreachableException;

public interface GrassrootPresenter {
    void handleResponseError(Response response);
    void handleNetworkConnectionError(Throwable t);
    void handleNetworkUploadError(Throwable t);
    void handleAuthenticationError(AuthenticationInvalidException t);
    void handleServerUnreachableError(ServerUnreachableException t);

}