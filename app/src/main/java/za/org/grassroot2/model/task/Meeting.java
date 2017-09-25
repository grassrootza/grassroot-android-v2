package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/20.
 */
@DatabaseTable(tableName = "meetings")
public class Meeting implements Task {

    public static final String ATTENDING = "RSVP_YES";
    public static final String NOT_ATTENDING = "RSVP_NO";

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private UUID uid;

    @DatabaseField
    private UUID parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    @DatabaseField(canBeNull = false)
    private String subject;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false)
    private String callerName;

    @DatabaseField(canBeNull = false)
    private String locationDescription;
    @DatabaseField
    private long createdDateTimeMillis;
    @DatabaseField
    private long meetingDateTimeMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @DatabaseField
    private String response;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

    @DatabaseField
    private boolean userPartOf = true;

    @DatabaseField
    private boolean publicMtg = false;

    @Override
    public UUID getUid() {
        return uid;
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
        return new Date(meetingDateTimeMillis);
    }

    @Override
    public String getName() {
        return subject;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.MEETING;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastChangeTimeServerMillis;
    }

    @Override
    public boolean hasResponded() {
        return !TextUtils.isEmpty(response);
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
        return publicMtg;
    }
}
