package za.org.grassroot2.model;

public class TokenResponse {

    private String userUid;
    private String msisdn;
    private String displayName;
    private String email;
    private String languageCode;
    private String systemRole;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getEmail() {
        return email;
    }

    public String getSystemRole() {
        return systemRole;
    }
}
