package za.org.grassroot.android.model;

import za.org.grassroot.android.model.enums.UploadableEntityType;

/**
 * Created by luke on 2017/08/16.
 * Used to abstract return result of sending an entity to server
 */

public class UploadResult {

    private final UploadableEntityType entityType;
    private final String localUid;
    private final String serverUid;
    private final EntityForUpload returnedEntity;
    private final Throwable uploadException;

    public UploadResult(final UploadableEntityType entityType, final String localUid, final String serverUid) {
        this.entityType = entityType;
        this.localUid = localUid;
        this.serverUid = serverUid;
        this.returnedEntity = null;
        this.uploadException = null;
    }

    public UploadResult(final UploadableEntityType entityType, EntityForUpload returnedEntity) {
        this.entityType = entityType;
        this.returnedEntity = returnedEntity;
        this.localUid = null;
        this.serverUid = null;
        this.uploadException = null;
    }

    public UploadResult(final UploadableEntityType entityType, final Throwable uploadException) {
        this.entityType = entityType;
        this.uploadException = uploadException;
        this.returnedEntity = null;
        this.serverUid = null;
        this.localUid = null;
    }

    public String getLocalUid() {
        return localUid;
    }

    public String getServerUid() {
        return serverUid;
    }

    public EntityForUpload getReturnedEntity() {
        return returnedEntity;
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "entityType=" + entityType +
                ", uploadException=" + (uploadException == null ? "none" : uploadException) +
                '}';
    }
}
