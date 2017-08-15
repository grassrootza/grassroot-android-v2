package za.org.grassroot.android.model;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 2017/08/15.
 * Holds the information needed to generate a LiveWire alert
 * todo: add destination information
 */

public class LiveWireAlert extends RealmObject implements EntityForUpload {

    @PrimaryKey
    private String uid;

    private String headline;
    private MediaFile mediaFile;
    private String description;

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
        taskUid = builder.taskUid;
        groupUid = builder.groupUid;
    }

    public static Builder newBuilder() {
        return new Builder();
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

        public LiveWireAlert build() {
            return new LiveWireAlert(this);
        }

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
}
