package za.org.grassroot2.presenter;

import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.NetworkUnavailableException;
import za.org.grassroot2.model.exception.ServerUnreachableException;

public interface GrassrootPresenter {

    void handleNetworkConnectionError(NetworkUnavailableException t);
    void handleAuthenticationError(AuthenticationInvalidException t);
    void handleServerUnreachableError(ServerUnreachableException t);

}
