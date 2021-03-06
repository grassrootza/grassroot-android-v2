package za.org.grassroot2.model.alert;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.ExcludeFromSerialization;
import za.org.grassroot2.model.HomeFeedItem;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForUpload;
import za.org.grassroot2.model.network.Syncable;

/**
 * Created by luke on 2017/08/15.
 * Holds the information needed to generate a LiveWire alert
 * todo: disposableOnDetach destination information
 */

@DatabaseTable(tableName = "livewire_alerts")
public class LiveWireAlert implements EntityForUpload, Syncable, AroundItem, HomeFeedItem {

    public static final String TYPE_GENERIC = "INSTANT"; // server calls it this, for legacy reasons
    public static final String TYPE_MEETING = "MEETING";

    @DatabaseField(id = true)
    private String uid;
    @DatabaseField
    private String serverUid;
    @DatabaseField
    private String headline;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "TEXT REFERENCES media_files(uid) ON DELETE CASCADE")
    private MediaFile mediaFile;
    @DatabaseField
    private String    description;
    @DatabaseField
    private String    alertType;
    @DatabaseField
    private String    taskUid;
    @DatabaseField
    private String    groupUid;
    @DatabaseField
    private boolean   complete;
    @DatabaseField
    private boolean   sending;
    @DatabaseField
    private boolean   underReview;
    @DatabaseField
    private String    creatingUserName;
    @DatabaseField
    private boolean   released;
    @DatabaseField
    private String    ancestorGroupName;
    @DatabaseField
    @SerializedName("creationDateMillis")
    private long      createdDate;
    @DatabaseField
    private transient boolean synced = true;

    @ExcludeFromSerialization
    private double longitude;

    @ExcludeFromSerialization
    private double latitude;

    public LiveWireAlert() {
        this.uid = UUID.randomUUID().toString();
        createdDate = System.currentTimeMillis();
    }

    private LiveWireAlert(Builder builder) {
        this();
        headline = builder.headline;
        mediaFile = builder.mediaFile;
        description = builder.description;
        alertType = builder.alertType;
        taskUid = builder.taskUid;
        groupUid = builder.groupUid;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.LIVE_WIRE_ALERT;
    }

    @Override
    public void initUploading() {
        this.sending = true;
    }

    @Override
    public void haltUploading(boolean uploadSucceded) {
        this.sending = false;
        if (!underReview) {
            this.underReview = uploadSucceded;
        }
    }

    @Override
    public boolean isToUpload() {
        return complete;
    }

    @Override
    public boolean isUploading() {
        return sending;
    }

    @Override
    public boolean isUploaded() {
        return complete && (underReview || released);
    }

    @Override
    public ArrayList<EntityForUpload> priorEntitiesToUpload() {
        if (mediaFile != null) {
            ArrayList<EntityForUpload> list = new ArrayList<>();
            list.add(mediaFile);
            return list;
        } else {
            return null;
        }
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public long createdDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
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

    @Override
    public long date() {
        return createdDate;
    }

    @Override
    public String searchableContent() {
        return headline + (description != null ? description : "");
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }

    public String getCreatingUserName() {
        return creatingUserName;
    }

    public void setCreatingUserName(String creatingUserName) {
        this.creatingUserName = creatingUserName;
    }

    public static final class Builder {

        private String headline;
        private MediaFile mediaFile;
        private String description;
        private String alertType;
        private String taskUid;
        private String groupUid;

        private Builder() {
        }

        public Builder headline(String val) {
            headline = val;
            return this;
        }

        public Builder mediaFile(MediaFile val) {
            mediaFile = val;
            return this;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder taskUid(String val) {
            taskUid = val;
            return this;
        }

        public Builder groupUid(String val) {
            groupUid = val;
            return this;
        }

        public Builder alertType(String val) {
            alertType = val;
            return this;
        }

        public LiveWireAlert build() {
            return new LiveWireAlert(this);
        }

    }

    public String getUid() {
        return uid;
    }

    public String getServerUid() {
        return serverUid;
    }

    public void setServerUid(String serverUid) {
        this.serverUid = serverUid;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskUid() {
        return taskUid;
    }

    public void setTaskUid(String taskUid) {
        this.taskUid = taskUid;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public void setUnderReview(boolean underReview) {
        this.underReview = underReview;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        validateAlertType(alertType);
        this.alertType = alertType;
    }

    public boolean areMinimumFieldsComplete() {
        return !TextUtils.isEmpty(headline) && !TextUtils.isEmpty(alertType) &&
                !(TextUtils.isEmpty(groupUid) && TextUtils.isEmpty(taskUid));
    }

    private void validateAlertType(String alertType) {
        if (!TYPE_GENERIC.equals(alertType) && !TYPE_MEETING.equals(alertType)) {
            throw new IllegalArgumentException("Error! Alert type must be one of standard types");
        }
    }

    public boolean hasMedia() {
        return mediaFile != null;
    }

    public Set<String> getMediaFileKeys() {
        return mediaFile == null ? null :
                Collections.singleton(mediaFile.getUid().toString());
    }

    @Override
    public String toString() {
        return "LiveWireAlert{" +
                "uid='" + uid + '\'' +
                ", headline='" + headline + '\'' +
                ", description='" + description + '\'' +
                ", alertType='" + alertType + '\'' +
                ", groupUid='" + groupUid + '\'' +
                '}';
    }
}
