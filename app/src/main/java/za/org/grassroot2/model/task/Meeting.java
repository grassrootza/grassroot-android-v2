package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;

@DatabaseTable(tableName = "meetings")
public class Meeting implements Task, Syncable {

    public static final String ATTENDING = "RSVP_YES";
    public static final String NOT_ATTENDING = "RSVP_NO";

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private String uid;

    @DatabaseField
    private String parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    @SerializedName("title")
    @DatabaseField(canBeNull = false)
    private String subject;

    @DatabaseField
    private String description;

    @SerializedName("createdByUserName")
    @DatabaseField
    private String callerName;
    @DatabaseField
    @SerializedName("location")
    private String locationDescription;

    @DatabaseField
    private long createdDateTimeMillis;
    @DatabaseField
    @SerializedName("deadlineMillis")
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

    @DatabaseField
    private transient boolean synced = true;

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getParentUid() {
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
    public long getDeadlineMillis() {
        return meetingDateTimeMillis;
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

    public String getCallerName() {
        return callerName;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setCreatedDateTimeMillis(long createdDateTimeMillis) {
        this.createdDateTimeMillis = createdDateTimeMillis;
    }

    public void setMeetingDateTimeMillis(long meetingDateTimeMillis) {
        this.meetingDateTimeMillis = meetingDateTimeMillis;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public long createdDate() {
        return createdDateTimeMillis;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "uid='" + uid + '\'' +
                ", parentUid='" + parentUid + '\'' +
                ", parentEntityType=" + parentEntityType +
                ", subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", callerName='" + callerName + '\'' +
                ", locationDescription='" + locationDescription + '\'' +
                ", createdDateTimeMillis=" + createdDateTimeMillis +
                ", meetingDateTimeMillis=" + meetingDateTimeMillis +
                ", lastChangeTimeServerMillis=" + lastChangeTimeServerMillis +
                ", response='" + response + '\'' +
                ", mediaFile=" + mediaFile +
                ", userPartOf=" + userPartOf +
                ", publicMtg=" + publicMtg +
                ", synced=" + synced +
                '}';
    }
}
