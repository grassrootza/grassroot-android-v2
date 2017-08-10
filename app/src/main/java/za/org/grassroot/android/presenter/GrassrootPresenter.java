package za.org.grassroot.android.presenter;

import za.org.grassroot.android.model.exception.AuthenticationInvalidException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.exception.ServerUnreachableException;

public interface GrassrootPresenter {

    void handleNetworkConnectionError(NetworkUnavailableException t);
    void handleAuthenticationError(AuthenticationInvalidException t);
    void handleServerUnreachableError(ServerUnreachableException t);

    void cleanUpForActivity(); // close realm, etc

}
