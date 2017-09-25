package za.org.grassroot2.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;

@DatabaseTable(tableName = "groups")
public class Group implements EntityForDownload, SelectableItem {

    /*
    Central, highly important entity, so a few notes:

    1. Major trade off on primary key. on the one hand this design means we are going to have to change the
    primary key, after we create a group and get it back from the server. on the other hand, using
    a different (local) primary key will mean we can't lean on Realm's copyToRealmUpdate, which
    is extremely useful when retrieving long lists. in the end, have decided that lesser of two
    evils is to occasionally have an expensive change-primary-key call when creating a group

    2. Distinction between 'heavy' and 'light' forms. For a large number of functions, all we have to know
    about a group is it's name, how many members it has, and what permissions the user has on it. For some
    heavier uses, e.g., member adds and views, we may need to know more

     */

    @DatabaseField(id = true)
    @SerializedName("groupUid")
    private UUID uid;

    @DatabaseField(canBeNull = false)
    @SerializedName("name")
    private String name;

    @DatabaseField(canBeNull = false)
    @SerializedName("memberCount")
    private Integer memberCount;

    @DatabaseField(canBeNull = false)
    @SerializedName("userRole")
    private String userRole;

    @DatabaseField
    @SerializedName("lastGroupChange")
    private long lastTimeChangedServer = 0;

    @DatabaseField
    @SerializedName("lastTaskOrChangeTime")
    private long lastActionOrChange = 0;

    @DatabaseField
    @SerializedName("permissions")
    private String permissions;

    public Group() {
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.GROUP;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return memberCount + " members";
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public long getLastActionOrChange() {
        return lastActionOrChange;
    }

    public void setLastTimeChangedServer(long lastTimeChangedServer) {
        this.lastTimeChangedServer = lastTimeChangedServer;
    }

    public void setLastActionOrChange(long lastActionOrChange) {
        this.lastActionOrChange = lastActionOrChange;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastTimeChangedServer;
    }

    @Override
    public String toString() {
        return "Group{" +
                "uid='" + uid + '\'' +
                // ", name='" + name + '\'' +
                // ", memberCount=" + memberCount +
                // ", userRole='" + userRole + '\'' +
                ", lastTimeChangedServer=" + lastTimeChangedServer +
                '}';
    }
}
