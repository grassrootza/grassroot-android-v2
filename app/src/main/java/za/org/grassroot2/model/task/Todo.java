package za.org.grassroot2.model.task;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/21.
 */
@DatabaseTable(tableName = "todos")
public class Todo implements Task {

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private String uid;

    @DatabaseField
    private String parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    @SerializedName("title")
    @DatabaseField(canBeNull = false)
    private String summary;

    @DatabaseField
    private String description;

    @SerializedName("createdByUserName")
    @DatabaseField
    private String recorderName;

    @DatabaseField
    private boolean needsConfirmation;

    @DatabaseField
    private boolean isConfirmed;

    @DatabaseField
    @SerializedName("createdDateTimeMillis")
    private long createdDate;
    @DatabaseField
    @SerializedName("deadlineMillis")
    private long deadlineDateTimeMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @DatabaseField
    private List<String> assignedMembers;

    @DatabaseField
    private List<String> confirmingMembers;

    @DatabaseField
    private boolean hasConfirmed;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

    @DatabaseField
    private boolean userPartOf = true;

    @DatabaseField
    private boolean publicTodo = false; // no such thing yet, but flexibility for future

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getParentUid() {
        return parentUid;
    }

    @Override
    public void setParentUid(String uid) {
        parentUid = uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public GrassrootEntityType getParentEntityType() {
        return parentEntityType;
    }

    @Override
    public Date getCreatedDateTime() {
        return new Date(createdDate);
    }

    @Override
    public long getDeadlineMillis() {
        return deadlineDateTimeMillis;
    }

    @Override
    public String getName() {
        return summary;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.TODO;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastChangeTimeServerMillis;
    }

    @Override
    public boolean hasResponded() {
        return hasConfirmed;
    }

    @Override
    public boolean hasMedia() {
        return mediaFile != null;
    }

    @Override
    public boolean isUserPartOf() {
        return userPartOf;
    }

    @Override
    public boolean isPublic() {
        return publicTodo;
    }

    public String getRecorderName() {
        return recorderName;
    }
}
