package za.org.grassroot2.model.task;

import java.util.Date;

import za.org.grassroot2.model.HomeFeedItem;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;

public interface Task extends EntityForDownload, SelectableItem, HomeFeedItem {

    String getParentUid();
    void setParentUid(String uid);
    void setUid(String uid);
    GrassrootEntityType getParentEntityType();

    Date getCreatedDateTime();
    long getDeadlineMillis();

    boolean hasResponded();
    boolean hasMedia();

    //String getTodoType();

    @Override
    String getDescription();

    boolean isUserPartOf();
    boolean isPublic();

}