package za.org.grassroot2.model;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class for storing details and preferences of current user
 */
@DatabaseTable(tableName = "user_profiles")
public class UserProfile {

    public static final int SYNC_STATE_NONE = 0;
    public static final int SYNC_STATE_COMPLETED = 1;
    public static final int SYNC_STATE_FAILED = 2;

    @DatabaseField(id = true)
    private String uid;

    @DatabaseField
    private String msisdn;

    @DatabaseField
    private String displayName;

    @DatabaseField
    private String emailAddress;

    @DatabaseField
    private String systemRole;

    @DatabaseField
    private int syncStatus = SYNC_STATE_NONE;

    public UserProfile() {
    }

    public UserProfile(String uid, String msisdn, String displayName, String emailAddress, String systemRole) {
        this.uid = uid;
        this.msisdn = msisdn;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.systemRole = systemRole;
    }

    public void updateFields(String uid, String msisdn, String name, String emailAddress, String role) {
        if (!TextUtils.isEmpty(uid)) {
            this.uid = uid;
        }
        if (!TextUtils.isEmpty(msisdn)) {
            this.msisdn = msisdn;
        }
        if (!TextUtils.isEmpty(name)) {
            this.displayName = name;
        }
        if (!TextUtils.isEmpty(emailAddress)) {
            this.emailAddress = emailAddress;
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public boolean isSyncComplete() {
        return syncStatus == SYNC_STATE_COMPLETED;
    }

    public boolean isSyncFailed() {
        return syncStatus == SYNC_STATE_FAILED;
    }

    public void setSyncState(int status) {
        this.syncStatus = status;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "uid='" + uid + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + emailAddress + '\'' +
                ", systemRole='" + systemRole + '\'' +
                ", syncStatus=" + syncStatus +
                '}';
    }

}
