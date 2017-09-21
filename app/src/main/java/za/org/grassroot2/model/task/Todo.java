package za.org.grassroot2.model.task;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/21.
 */

public class Todo implements Task {

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private UUID uid;

    @DatabaseField
    private UUID parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    @DatabaseField(canBeNull = false)
    private String summary;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false)
    private String recorderName;

    @DatabaseField
    private boolean needsConfirmation;

    @DatabaseField
    private boolean isConfirmed;

    @DatabaseField
    private long createdDateTimeMillis;
    @DatabaseField
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

    @Override
    public String getUid() {
        return uid.toString();
    }

    @Override
    public UUID getParentUid() {
        return parentUid;
    }

    @Override
    public GrassrootEntityType getParentEntityType() {
        return parentEntityType;
    }

    @Override
    public Date getCreatedDateTime() {
        return new Date(createdDateTimeMillis);
    }

    @Override
    public Date getDeadlineDateTime() {
        return new Date(deadlineDateTimeMillis);
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
        return 0;
    }

    @Override
    public boolean hasResponded() {
        return hasConfirmed;
    }

    @Override
    public boolean hasMedia() {
        return mediaFile != null;
    }
}
