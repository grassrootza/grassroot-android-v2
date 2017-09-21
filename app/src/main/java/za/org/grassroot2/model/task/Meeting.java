package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;
import java.util.UUID;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/20.
 */

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
}
