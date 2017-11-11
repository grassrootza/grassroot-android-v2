package za.org.grassroot2.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import za.org.grassroot2.model.enums.GrassrootEntityType;

public class AroundEntity implements HomeFeedItem {

    @DatabaseField(id = true)
    private String uid;

    @DatabaseField
    private String contactName;

    @DatabaseField
    private String description;

    @DatabaseField
    private String fetchingUserIsMember;

    @DatabaseField
    private Double latitude;

    @DatabaseField
    private Double longitude;

    @DatabaseField
    private String title;

    @DatabaseField
    private String ancestorGroupName;

    @DatabaseField
    @SerializedName("entityDateTimeInMillis")
    private long createdDate;

    @DatabaseField
    private GrassrootEntityType type;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFetchingUserIsMember() {
        return fetchingUserIsMember;
    }

    public void setFetchingUserIsMember(String fetchingUserIsMember) {
        this.fetchingUserIsMember = fetchingUserIsMember;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GrassrootEntityType getType() {
        return type;
    }

    public void setType(GrassrootEntityType type) {
        this.type = type;
    }

    @Override
    public long date() {
        return createdDate;
    }

    @Override
    public String searchableContent() {
        return (title != null ? title : "") + (description != null ? description : "");
    }

    public String getAncestorGroupName() {
        return ancestorGroupName;
    }

    public void setAncestorGroupName(String ancestorGroupName) {
        this.ancestorGroupName = ancestorGroupName;
    }
}
