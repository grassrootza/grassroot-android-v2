package za.org.grassroot.android.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import za.org.grassroot.android.model.enums.NetworkEntityType;
import za.org.grassroot.android.model.network.EntityForUpload;

/**
 * Created by luke on 2017/08/15.
 * Holds the information needed to generate a LiveWire alert
 * todo: add destination information
 */

public class LiveWireAlert extends RealmObject implements EntityForUpload {

    public static final String TYPE_GENERIC = "INSTANT"; // server calls it this, for legacy reasons
    public static final String TYPE_MEETING = "MEETING";

    @PrimaryKey
    private String uid;
    private String serverUid;

    private String headline;
    private MediaFile mediaFile;
    private String description;

    private String alertType;
    private String taskUid;
    private String groupUid;

    private boolean complete;
    private boolean sending;
    private boolean underReview;
    private boolean released;

    public LiveWireAlert() {
        // for Realm/Dagger
        this.uid = UUID.randomUUID().toString();
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
    public NetworkEntityType getType() {
        return NetworkEntityType.LIVEWIRE_ALERT;
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

    @Override
    public String toString() {
        return "LiveWireAlert{" +
                "uid='" + uid + '\'' +
                ", headline='" + headline + '\'' +
                '}';
    }
}
