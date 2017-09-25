package za.org.grassroot2.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

/**
 * Created by luke on 2017/09/25.
 * Entity to hold a group that appears on 'around me', but which the user is not part of
 */
@DatabaseTable(tableName = "public_groups")
public class PublicGroup {

    @DatabaseField(id = true)
    @SerializedName("groupUid")
    private UUID uid;

    @DatabaseField(canBeNull = false)
    @SerializedName("name")
    private String name;

    @DatabaseField(canBeNull = false)
    @SerializedName("memberCount")
    private Integer memberCount;

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
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
}
