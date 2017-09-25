package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/09/21.
 */
@DatabaseTable(tableName = "votes")
public class Vote implements Task {

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private UUID uid;

    @DatabaseField
    private UUID parentUid;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    // note: watch out for nullable here, may want to allow it depending on user route (once NLU etc in place)
    @DatabaseField(canBeNull = false)
    private String subject;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false)
    private String callerName;

    @DatabaseField
    private long createdDateTimeMillis;
    @DatabaseField
    private long closingDateTimeMillis;
    @DatabaseField
    private long lastChangeTimeServerMillis;

    @DatabaseField
    private List<String> voteOptions;
    @DatabaseField
    private String voteResponse;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

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
    public Date getDeadlineDateTime() {
        return new Date(closingDateTimeMillis);
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
}
