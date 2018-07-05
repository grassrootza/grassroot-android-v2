package za.org.grassroot2.model.request;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

import za.org.grassroot2.model.network.Syncable;

@DatabaseTable(tableName = "member_requests")
public class MemberRequest implements Syncable {

    @DatabaseField(generatedId = true)
    public transient long id;

    @DatabaseField
    public transient String groupUid;

    @DatabaseField
    public List<String> alternateNumbers = new ArrayList<>();

    @DatabaseField
    public String displayName;

    @DatabaseField
    public String emailAddress;

    @DatabaseField
    public String phoneNumber;

    @DatabaseField
    public String roleName;

    @DatabaseField
    public transient long createdDate;

    @Override
    public boolean isSynced() {
        return false;
    }

    @Override
    public long createdDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "MemberRequest{" +
                "id=" + id +
                ", groupUid='" + groupUid + '\'' +
                ", alternateNumbers=" + alternateNumbers +
                ", displayName='" + displayName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", roleName='" + roleName + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
