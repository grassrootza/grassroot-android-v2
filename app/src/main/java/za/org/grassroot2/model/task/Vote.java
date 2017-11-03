package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import za.org.grassroot2.model.HomeFeedItem;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/21.
 */
@DatabaseTable(tableName = "votes")
public class Vote implements Task {

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
    @SerializedName("createdDateTimeMillis")
    private long createdDate;

    @DatabaseField
    @SerializedName("deadlineMillis")
    private long deadlineMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @DatabaseField
    private String ancestorGroupName;

    @SerializedName("voteResults")
    @DatabaseField
    private HashMap<String, Integer> voteOptions;

    @DatabaseField
    private String voteResponse;

    @DatabaseField
    private List<String> tags;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

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

    public HashMap<String, Integer> getVoteOptions() {
        return voteOptions;
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }
}
