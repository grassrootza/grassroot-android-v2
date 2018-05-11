package za.org.grassroot2.model.task;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.org.grassroot2.model.HomeFeedItem;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;

/**
 * Created by luke on 2017/09/21.
 */
@DatabaseTable(tableName = "todos")
public class Todo implements Task, Syncable {

    @DatabaseField(id = true)
    @SerializedName("taskUid")
    private String uid;

    public static final String TODO_YES = "YES";
    public static final String TODO_NO = "NO";
    public static String TODO_INFO = ""; // value assigned at runtime

    @DatabaseField
    private String parentUid;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @DatabaseField
    private String response;

    @SerializedName("todoResponses")
    @DatabaseField
    private Map<String, String> todoResponses;

    @DatabaseField
    @SerializedName("location")
    private String locationDescription;

    @DatabaseField
    private String todoType;

    @DatabaseField
    private transient boolean synced = true;

    @DatabaseField
    private GrassrootEntityType parentEntityType;

    @SerializedName("title")
    @DatabaseField(canBeNull = false)
    private String subject;

    @Override
    public String toString() {
        return "Todo{" +
                "uid='" + uid + '\'' +
                ", parentUid='" + parentUid + '\'' +
                ", response='" + response + '\'' +
                ", todoResponses=" + todoResponses +
                ", locationDescription='" + locationDescription + '\'' +
                ", todoType='" + todoType + '\'' +
                ", synced=" + synced +
                ", parentEntityType=" + parentEntityType +
                ", summary='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", recorderName='" + recorderName + '\'' +
                ", needsConfirmation=" + needsConfirmation +
                ", isConfirmed=" + isConfirmed +
                ", createdDate=" + createdDate +
                ", deadlineMillis=" + deadlineMillis +
                ", lastChangeTimeServerMillis=" + lastChangeTimeServerMillis +
                ", ancestorGroupName='" + ancestorGroupName + '\'' +
                ", assignedMembers=" + assignedMembers +
                ", confirmingMembers=" + confirmingMembers +
                ", hasConfirmed=" + hasConfirmed +
                ", mediaFile=" + mediaFile +
                ", userPartOf=" + userPartOf +
                ", publicTodo=" + publicTodo +
                '}';
    }

    @DatabaseField
    private String description;

    @SerializedName("createdByUserName")
    @DatabaseField
    private String recorderName;

    @DatabaseField
    private boolean needsConfirmation;

    @DatabaseField
    private String userResponse;

    @DatabaseField
    private boolean isConfirmed;

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

    @DatabaseField
    private List<String> assignedMembers;

    @DatabaseField
    private List<String> confirmingMembers;

    @DatabaseField
    private boolean hasConfirmed;

    @DatabaseField(foreign = true)
    private MediaFile mediaFile;

    @DatabaseField
    private boolean userPartOf = true;

    @DatabaseField
    private boolean publicTodo = false; // no such thing yet, but flexibility for future

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
    public Date getCreatedDateTime() {
        return new Date(createdDate);
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String todoResponse) {
        this.userResponse = userResponse;
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
        return GrassrootEntityType.TODO;
    }

    @Override
    public long getLastTimeChangedServer() {
        return lastChangeTimeServerMillis;
    }

    @Override
    public boolean hasResponded() {
        return hasConfirmed;
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    // @Override
    public String getTodoType() { return todoType; }

    public void setTodoType() {
        this.todoType = todoType;
    }

    @Override
    public long createdDate() {
        return createdDate;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
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
        return publicTodo;
    }

    public String getRecorderName() {
        return recorderName;
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }

    public Map<String, String> getTodoResponses() {
        return todoResponses;
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
