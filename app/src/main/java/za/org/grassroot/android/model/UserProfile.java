package za.org.grassroot.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 2017/07/06.
 * Class for storing details and preferences of current user
 */
public class UserProfile extends RealmObject {

    @PrimaryKey
    private String uid;

    private String msisdn;
    private String displayName;

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
}
