package za.org.grassroot2.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import za.org.grassroot2.model.enums.NetworkEntityType;
import za.org.grassroot2.model.helper.RealmString;
import za.org.grassroot2.model.network.EntityForDownload;

public class Group extends RealmObject implements EntityForDownload, SelectableItem {

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

    @PrimaryKey
    @Required
    @SerializedName("groupUid")
    private String uid;

    @Required
    @SerializedName("name")
    private String name;

    @Required
    @SerializedName("memberCount")
    private Integer memberCount;

    @Required
    @SerializedName("userRole")
    private String userRole;

    @SerializedName("lastGroupChange")
    private long lastTimeChangedServer = 0;

    @SerializedName("lastTaskOrChangeTime")
    private long lastActionOrChange = 0;

    @SerializedName("permissions")
    private RealmList<RealmString> permissions;

    public Group() {
        // for Realm
    }

    @Override
    public NetworkEntityType getType() {
        return NetworkEntityType.GROUP;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        // todo: make this return something meaningful
        return userRole + ", " + memberCount + " members";
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

    public RealmList<RealmString> getPermissions() {
        return permissions;
    }

    public void setPermissions(RealmList<RealmString> permissions) {
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
    public RealmObject getRealmObject() {
        return this;
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
