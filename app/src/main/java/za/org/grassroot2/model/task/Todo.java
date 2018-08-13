package za.org.grassroot2.model.task;

import android.location.Location;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.reflect.Array;
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

    @DatabaseField
    private String parentUid;

    @DatabaseField
    private String response;

    //@SerializedName("todoResponses")
    //@DatabaseField
    //private Map<String, String> todoResponses;

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

    @DatabaseField
    @SerializedName("recurringPeriodMillis")
    private long recurringPeriodMillis = 1440;

    @DatabaseField
    @SerializedName("responseTag")
    private String responseTag;

    @DatabaseField
    @SerializedName("confirmingMemberUids")
    private List<String> confirmingMemberUids;

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
    @SerializedName("assignedMemberUids")
    private List<String> assignedMemberUids;

    @DatabaseField
    @SerializedName("mediaFileUids")
    private List<String> mediaFileUids;

    @DatabaseField
    @SerializedName("recurring")
    private boolean recurring;

    @DatabaseField
    @SerializedName("requireImages")
    private boolean requireImages;

    public static String getTodoNo() {
        return TODO_NO;
    }

    public boolean isRequireImages() {
        return requireImages;
    }

    public void setRequireImages(boolean requireImages) {
        this.requireImages = requireImages;
    }

    public static String getTodoYes() {
        return TODO_YES;
    }

    public List<String> getAssignedMemberUids() {
        return assignedMemberUids;
    }

    public void setAssignedMemberUids(List assignedMemberUids) {
        this.assignedMemberUids = assignedMemberUids;
    }

    public void setDeadlineMillis(long deadlineMillis) {
        this.deadlineMillis = deadlineMillis;
    }

    public List<String> getMediaFileUids() {
        return mediaFileUids;
    }

    public void setMediaFileUid(List mediaFileUid) {
        this.mediaFileUids = mediaFileUid;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public long getRecurringPeriodMillis() {
        return recurringPeriodMillis;
    }

    public void setRecurringPeriodMillis(long recurringPeriodMillis) {
        this.recurringPeriodMillis = recurringPeriodMillis;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getResponseTag() {
        return responseTag;
    }

    public void setResponseTag(String responseTag) {
        this.responseTag = responseTag;
    }

    public List<String> getConfirmingMemberUids() {
        return confirmingMemberUids;
    }

    public void setConfirmingMemberUids(List confirmingMemberUids) {
        this.confirmingMemberUids = confirmingMemberUids;
    }

    public List<String> getConfirmingMembers() {
        return confirmingMembers;
    }

    public void setConfirmingMembers(List<String> confirmingMembers) {
        this.confirmingMembers = confirmingMembers;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

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

    public void setTodoType(String todoType) {
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

    @Override
    public void setLocation(Location location) {
//        this.longitude = location.getLongitude();
//        this.latitude = location.getLatitude();
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

    @Override
    public long date() {
        return deadlineMillis;
    }

    @Override
    public String searchableContent() {
        return subject + (description != null ? description : "");
    }

    @Override
    public String toString() {
        return "Todo{" +
                "uid='" + uid + '\'' +
                ", parentUid='" + parentUid + '\'' +
                ", response='" + response + '\'' +
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
}
