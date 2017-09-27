package za.org.grassroot2.model.request;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/25.
 */
@DatabaseTable(tableName = "group_join_request")
public class GroupJoinRequest implements Request {

    @DatabaseField(id = true)
    @SerializedName("requestUid")
    private String uid;
    @DatabaseField(canBeNull = false)
    @SerializedName("groupUid")
    private UUID groupUid;
    @DatabaseField(canBeNull = false)
    private String requestorName;
    @DatabaseField(canBeNull = false)
    private String status;

    @DatabaseField
    private String description;

    @DatabaseField
    private long createdDateTimeMillis;
    @DatabaseField
    private long processedDateTimeMillis;
    @DatabaseField
    private long lastTimeChangedServerMillis;

    private boolean generatedByThisUser;

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.GROUP_JOIN_REQUEST;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastTimeChangedServerMillis;
    }

    public UUID getGroupUid() {
        return groupUid;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean isThisUserGenerated() {
        return generatedByThisUser;
    }

    @Override
    public boolean canThisUserApprove() {
        return !generatedByThisUser; // likely more complex in future
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedDateTimeMillis() {
        return createdDateTimeMillis;
    }

    public long getProcessedDateTimeMillis() {
        return processedDateTimeMillis;
    }

    public long getLastTimeChangedServerMillis() {
        return lastTimeChangedServerMillis;
    }

    public boolean isGeneratedByThisUser() {
        return generatedByThisUser;
    }


}