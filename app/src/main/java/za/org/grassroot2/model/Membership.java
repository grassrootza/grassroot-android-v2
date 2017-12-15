package za.org.grassroot2.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;

/**
 * Created by luke on 2017/12/10.
 */

@DatabaseTable(tableName = "memberships")
public class Membership implements EntityForDownload, SelectableItem, Serializable {

    @DatabaseField(id = true)
    @SerializedName("compositeUid")
    private String compositeUid;

    @DatabaseField(foreign = true)
    private Group group;

    @DatabaseField(canBeNull = false)
    @SerializedName("groupUid")
    private String groupUid;

    @DatabaseField(canBeNull = false)
    @SerializedName("userUid")
    private String userUid;

    @DatabaseField
    @SerializedName("phoneNumber")
    private String phoneNumber;

    @DatabaseField
    @SerializedName("memberEmail")
    private String emailAddress;

    @DatabaseField
    @SerializedName("roleName")
    private String roleName;

    @DatabaseField
    @SerializedName("displayName")
    private String displayName;

    @DatabaseField
    @SerializedName("joinedDateTimeMillis")
    private long joinedTimeMillis = 0;

    public Membership() {
        // for various libraries
    }

    @Override
    public String getName() {
        return !TextUtils.isEmpty(displayName) ? displayName :
            !TextUtils.isEmpty(phoneNumber) ? phoneNumber :
            !TextUtils.isEmpty(emailAddress) ? emailAddress : "Unknown";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.MEMBERSHIP;
    }

    @Override
    public String getUid() {
        return compositeUid;
    }

    @Override
    public long getLastTimeChangedServer() {
        return joinedTimeMillis;
    }

    // for filtering
    public boolean containsString(CharSequence constraint) {
        return (displayName != null && displayName.toLowerCase().contains(constraint.toString().toLowerCase()))
            || (phoneNumber != null && phoneNumber.toLowerCase().contains(constraint.toString().toLowerCase()));
    }

    public String getCompositeUid() {
        return compositeUid;
    }

    public void setCompositeUid(String compositeUid) {
        this.compositeUid = compositeUid;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getJoinedTimeMillis() {
        return joinedTimeMillis;
    }

    public void setJoinedTimeMillis(long joinedTimeMillis) {
        this.joinedTimeMillis = joinedTimeMillis;
    }
}
