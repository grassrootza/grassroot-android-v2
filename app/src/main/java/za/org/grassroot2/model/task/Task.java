package za.org.grassroot2.model.task;

import java.util.Date;
import java.util.UUID;

import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.model.network.Syncable;

public interface Task extends EntityForDownload, SelectableItem {

    String getParentUid();
    void setParentUid(String uid);
    void setUid(String uid);
    GrassrootEntityType getParentEntityType();

    Date getCreatedDateTime();
    long getDeadlineMillis();

    boolean hasResponded();
    boolean hasMedia();

    boolean isUserPartOf();
    boolean isPublic();

}