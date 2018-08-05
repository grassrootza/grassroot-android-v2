package za.org.grassroot2.services.account

interface AuthConstants {
    companion object {
        const val ACCOUNT_TYPE = "za.org.grassroot2"
        const val ACCOUNT_NAME = "Grassroot"
        const val AUTH_TOKENTYPE = ACCOUNT_TYPE

        // for calls to auth server
        const val AUTH_CLIENT_TYPE = "ANDROID"
        const val USER_DATA_CURRENT_TOKEN = "current_token"
        const val USER_DATA_LOGGED_IN = "logged_in"
    }
}
