package za.org.grassroot2.model.task;

public class PendingTodoDTO {

    public boolean hasPendingResponse;

    public String entityType;
    public String entityUid;

    public String title;
    public String description;

    public String parentName;

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityUid() {
        return entityUid;
    }

    @Override
    public String toString() {
        return "PendingTodoDTO{" +
                "hasPendingResponse=" + hasPendingResponse +
                ", entityType='" + entityType + '\'' +
                ", entityUid='" + entityUid + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", parentName='" + parentName + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", dueByTimestampMillis=" + dueByTimestampMillis +
                '}';
    }

    public void setEntityUid(String entityUid) {
        this.entityUid = entityUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public long getDueByTimestampMillis() {
        return dueByTimestampMillis;
    }

    public void setDueByTimestampMillis(long dueByTimestampMillis) {
        this.dueByTimestampMillis = dueByTimestampMillis;
    }

    public String creatorName;

    public long dueByTimestampMillis;

    public boolean isHasPendingResponse() {
        return hasPendingResponse;
    }

    public void setHasPendingResponse(boolean hasPendingResponse) {
        this.hasPendingResponse = hasPendingResponse;
    }

    public void PendingResponseDTO() {
        this.hasPendingResponse = false;
    }

}


