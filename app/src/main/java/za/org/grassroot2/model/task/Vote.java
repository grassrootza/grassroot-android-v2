package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;

/**
 * Created by luke on 2017/09/21.
 */
@DatabaseTable(tableName = "votes")
public class Vote implements Task, Syncable {

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private String uid;

    @DatabaseField
    private String parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    // note: watch out for nullable here, may want to allow it depending on user route (once NLU etc in place)
    @SerializedName("title")
    @DatabaseField(canBeNull = false)
    private String subject;

    @DatabaseField
    private String description;

    @SerializedName("createdByUserName")
    @DatabaseField
    private String callerName;

    @DatabaseField
    @SerializedName("createdDate")
    private long createdDate;

    @DatabaseField
    @SerializedName("mediaFileUid")
    private String mediaFileUid;

    public void setParentEntityType(GrassrootEntityType parentEntityType) {
        this.parentEntityType = parentEntityType;
    }

    public String getMediaFileUid() {
        return mediaFileUid;
    }

    public void setMediaFileUid(String mediaFileUid) {
        this.mediaFileUid = mediaFileUid;
    }

    public Array getAssignedMemberUids() {
        return assignedMemberUids;
    }

    public void setAssignedMemberUids(Array assignedMemberUids) {
        this.assignedMemberUids = assignedMemberUids;
    }

    @DatabaseField
    @SerializedName("assignedMemberUids")
    private Array assignedMemberUids;

    @DatabaseField
    @SerializedName("voteOptions")
    private Array voteOptions;

    public void setVoteOptions(Array voteOptions) {
        this.voteOptions = voteOptions;
    }

    public Array getVoteOptions() {
        return voteOptions;
    }

    @DatabaseField

    @SerializedName("deadlineMillis")
    private long deadlineMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @DatabaseField
    private String ancestorGroupName;

    @SerializedName("voteResults")
    @DatabaseField
    private HashMap<String, Integer> voteResults;

    @DatabaseField
    private String voteResponse;

    @DatabaseField
    private List<String> tags;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

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
    public String getName() {
        return subject;
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    @Override
    public long createdDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.VOTE;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastChangeTimeServerMillis;
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
    public long date() {
        return deadlineMillis;
    }

    @Override
    public String searchableContent() {
        return subject + (description != null ? description : "");
    }

    @Override
    public boolean hasResponded() {
        return !TextUtils.isEmpty(voteResponse);
    }

    @Override
    public boolean hasMedia() {
        return mediaFile != null;
    }

    @Override
    public boolean isUserPartOf() {
        // by definition votes are private, hence always true
        return true;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getVoteResponse() {
        return voteResponse;
    }

    public void setVoteResponse(String voteResponse) {
        this.voteResponse = voteResponse;
    }

    @Override
    public boolean isPublic() {
        return false; // see above
    }

    public List<String> getTags() {
        return tags;
    }

    public String getCallerName() {
        return callerName;
    }

    public HashMap<String, Integer> getVoteResults() {
        return voteResults;
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }

}
