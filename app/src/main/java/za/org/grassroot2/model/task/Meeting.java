package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.ExcludeFromSerialization;
import za.org.grassroot2.model.HomeFeedItem;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.Post;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;

@DatabaseTable(tableName = "meetings")
public class Meeting implements Task, Syncable, AroundItem {

    public static final String RSVP_YES = "YES";
    public static final String RSVP_MAYBE = "MAYBE";
    public static final String RSVP_NO = "NO";

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
    @SerializedName("createdDate")
    private long createdDate;
    @DatabaseField
    @SerializedName("deadlineMillis")
    private long deadlineMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @ForeignCollectionField
    private ForeignCollection<Post> posts;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @DatabaseField
    private String response;

    @DatabaseField
    private String ancestorGroupName;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

    @DatabaseField
    private boolean userPartOf = true;

    @DatabaseField
    private boolean publicMtg = false;

    @DatabaseField
    private transient boolean synced = true;

    @ExcludeFromSerialization
    private double longitude;

    @ExcludeFromSerialization
    private double latitude;

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
        return new Date(createdDate);
    }

    @Override
    public long getDeadlineMillis() {
        return deadlineMillis;
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

    @Override
    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
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
        return createdDate;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    @Override
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
                ", createdDate=" + createdDate +
                ", deadlineMillis=" + deadlineMillis +
                ", lastChangeTimeServerMillis=" + lastChangeTimeServerMillis +
                ", response='" + response + '\'' +
                ", mediaFile=" + mediaFile +
                ", userPartOf=" + userPartOf +
                ", publicMtg=" + publicMtg +
                ", synced=" + synced +
                '}';
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }

    @Override
    public long date() {
        return deadlineMillis;
    }

    @Override
    public String searchableContent() {
        return subject + (description != null ? description : "");
    }
}
