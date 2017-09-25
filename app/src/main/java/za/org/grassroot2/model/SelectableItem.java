package za.org.grassroot2.model;

import java.util.UUID;

/**
 * Created by luke on 2017/08/20.
 */

public interface SelectableItem {

    UUID getUid();
    String getName();
    String getDescription();

}
