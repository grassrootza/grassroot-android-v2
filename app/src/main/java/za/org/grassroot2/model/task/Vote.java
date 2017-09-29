package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    @DatabaseField(canBeNull = false)
    private String callerName;

    @DatabaseField
    private long createdDateTimeMillis;

    @DatabaseField
    @SerializedName("deadlineMillis")
    private long closingDateTimeMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @SerializedName("voteCount")
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
        return new Date(createdDateTimeMillis);
    }

    @Override
    public long getDeadlineMillis() {
        return closingDateTimeMillis;
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
}
