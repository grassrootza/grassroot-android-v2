package za.org.grassroot2.model.task;

import java.util.Date;
import java.util.UUID;

import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;

/**
 * Created by luke on 2017/09/20.
 */

public interface Task extends EntityForDownload, SelectableItem {

    UUID getParentUid();
    GrassrootEntityType getParentEntityType();

    Date getCreatedDateTime();
    Date getDeadlineDateTime();

    boolean hasResponded();
    boolean hasMedia();

    boolean isUserPartOf();
    boolean isPublic();

}