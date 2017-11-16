package za.org.grassroot2.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import za.org.grassroot2.model.task.Meeting;

@DatabaseTable(tableName = "posts")
public class Post {

    @DatabaseField
    private String actionLogType;
    @DatabaseField
    private boolean analyzed;
    @DatabaseField
    private String bucket;
    @DatabaseField
    private String caption;
    @DatabaseField
    private boolean countModified;
    @DatabaseField
    private long creationTime;
    @DatabaseField(id = true)
    private String key;
    @DatabaseField
    private double latitude;
    @DatabaseField
    private double longitude;
    @DatabaseField
    private String md5;
    @DatabaseField
    private int numberFaces;
    @DatabaseField
    private int revisedFaces;
    @DatabaseField
    private long storageTime;
    @DatabaseField
    private String taskUid;
    @DatabaseField
    private String userDisplayName;
    @DatabaseField
    private String userPhoneNumber;

    @DatabaseField(foreign = true)
    private Meeting meeting;

    public String getActionLogType() {
        return actionLogType;
    }

    public void setActionLogType(String actionLogType) {
        this.actionLogType = actionLogType;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isCountModified() {
        return countModified;
    }

    public void setCountModified(boolean countModified) {
        this.countModified = countModified;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getNumberFaces() {
        return numberFaces;
    }

    public void setNumberFaces(int numberFaces) {
        this.numberFaces = numberFaces;
    }

    public int getRevisedFaces() {
        return revisedFaces;
    }

    public void setRevisedFaces(int revisedFaces) {
        this.revisedFaces = revisedFaces;
    }

    public long getStorageTime() {
        return storageTime;
    }

    public void setStorageTime(long storageTime) {
        this.storageTime = storageTime;
    }

    public String getTaskUid() {
        return taskUid;
    }

    public void setTaskUid(String taskUid) {
        this.taskUid = taskUid;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}
