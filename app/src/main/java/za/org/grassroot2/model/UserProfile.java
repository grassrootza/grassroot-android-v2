package za.org.grassroot2.model;

import android.text.TextUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Class for storing details and preferences of current user
 */
public class UserProfile extends RealmObject {

    @PrimaryKey
    private int id = 0;

    private String uid;
    private String msisdn;
    private String displayName;
    private String systemRole;

    public UserProfile() {
        // for Android / Realm
    }

    public UserProfile(String uid, String msisdn, String displayName, String systemRole) {
        this.uid = uid;
        this.msisdn = msisdn;
        this.displayName = displayName;
        this.systemRole = systemRole;
    }

    public void updateFields(String uid, String msisdn, String name, String role) {
        if (!TextUtils.isEmpty(uid)) {
            this.uid = uid;
        }
        if (!TextUtils.isEmpty(msisdn)) {
            this.msisdn = msisdn;
        }
        if (!TextUtils.isEmpty(name)) {
            this.displayName = name;
        }
        if (!TextUtils.isEmpty(role)) {
            this.systemRole = role;
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", displayName='" + displayName + '\'' +
                ", systemRole='" + systemRole + '\'' +
                '}';
    }
}
