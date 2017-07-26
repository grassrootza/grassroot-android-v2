package za.org.grassroot.android.model;

/**
 * Created by luke on 2017/07/26.
 */

public class TokenResponse {

    private String userUid;
    private String msisdn;
    private String token;

    public String getMsisdn() {
        return msisdn;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getToken() {
        return token;
    }

}
