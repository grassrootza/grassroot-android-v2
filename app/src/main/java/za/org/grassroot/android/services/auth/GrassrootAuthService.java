package za.org.grassroot.android.services.auth;

/**
 * Created by luke on 2017/08/08.
 */

public interface GrassrootAuthService {

    String getToken();
    void logoutCleanSweap();
    void logoutRetainData();


}
