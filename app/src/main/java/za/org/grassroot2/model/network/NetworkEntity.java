package za.org.grassroot2.model.network;

import za.org.grassroot2.model.enums.GrassrootEntityType;

/**
 * Created by luke on 2017/08/17.
 */

public interface NetworkEntity {

    GrassrootEntityType getType();
    String getUid();
}
