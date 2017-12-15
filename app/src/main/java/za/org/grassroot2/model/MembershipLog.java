package za.org.grassroot2.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by luke on 2017/12/15.
 */
@DatabaseTable(tableName = "membership_logs")
public class MembershipLog {

    @DatabaseField(id = true)
    @SerializedName("groupLogId")
    private long groupLogId;

    @DatabaseField(foreign = true)
    private Group group;

    @DatabaseField(canBeNull = false)
    @SerializedName("groupUid")
    private String groupUid;

    @DatabaseField
    @SerializedName("userUid")
    private String userUid;

    @DatabaseField
    @SerializedName("memberName")
    private String memberName;

    @DatabaseField
    @SerializedName("roleName")
    private String roleName;

    @DatabaseField
    @SerializedName("changedDateTimeMillis")
    private long changeDateTimeMillis;

    @DatabaseField
    @SerializedName("changeType")
    private String changeType;

    @DatabaseField
    @SerializedName("changingUserName")
    private String changingUserName;

    @DatabaseField
    @SerializedName("description")
    private String description;


    public MembershipLog() {
        // for various libraries, as usual
    }

    public long getGroupLogId() {
        return groupLogId;
    }

    public void setGroupLogId(long groupLogId) {
        this.groupLogId = groupLogId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getChangeDateTimeMillis() {
        return changeDateTimeMillis;
    }

    public void setChangeDateTimeMillis(long changeDateTimeMillis) {
        this.changeDateTimeMillis = changeDateTimeMillis;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangingUserName() {
        return changingUserName;
    }

    public void setChangingUserName(String changingUserName) {
        this.changingUserName = changingUserName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // for filtering, todo: think about how to handle changeType
    public boolean containsString(CharSequence constraint) {
        return (memberName != null && memberName.toLowerCase().contains(constraint.toString().toLowerCase()))
            || (changingUserName != null && changingUserName.toLowerCase().contains(constraint.toString().toLowerCase()))
            || (description != null && description.toLowerCase().contains(constraint.toString().toLowerCase()))
            || (changeType != null && changeType.toLowerCase().contains(constraint.toString().toLowerCase()));
    }
}
