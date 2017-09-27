package za.org.grassroot2.model.alert;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;

/**
 * Created by luke on 2017/09/25.
 */
@DatabaseTable(tableName = "safety_alerts")
public class SafetyAlert implements EntityForDownload {

    @DatabaseField(id = true)
    @SerializedName("uid")
    private String uid;

    @DatabaseField(canBeNull = false)
    private long createdDateTimeMillis;

    @DatabaseField
    private long lastTimeChangedServerMillis;

    @DatabaseField(canBeNull = false)
    private String callingUserName;

    @DatabaseField
    private String description; // can be null

    @DatabaseField
    private boolean responded;

    @DatabaseField
    private boolean falseAlarm;


    @Override
    public GrassrootEntityType getType() {
        return GrassrootEntityType.SAFETY_ALERT;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastTimeChangedServerMillis;
    }

    public long getCreatedDateTimeMillis() {
        return createdDateTimeMillis;
    }

    public String getCallingUserName() {
        return callingUserName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isResponded() {
        return responded;
    }

    public boolean isFalseAlarm() {
        return falseAlarm;
    }
}
