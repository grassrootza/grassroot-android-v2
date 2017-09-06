package za.org.grassroot2.model.network;

import za.org.grassroot2.model.enums.NetworkEntityType;

/**
 * Created by luke on 2017/08/17.
 */

public interface NetworkEntity {

    NetworkEntityType getType();
    String getUid();

}
