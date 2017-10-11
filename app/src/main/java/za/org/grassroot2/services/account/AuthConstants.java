package za.org.grassroot2.services.account;

public interface AuthConstants {
    String ACCOUNT_TYPE = "za.org.grassroot2";
    String ACCOUNT_NAME = "Grassroot";
    String AUTH_TOKENTYPE = ACCOUNT_TYPE;

    // for calls to auth server
    String AUTH_CLIENT_TYPE        = "ANDROID";
    String USER_DATA_CURRENT_TOKEN = "current_token";
    String USER_DATA_LOGGED_IN     = "logged_in";
}
