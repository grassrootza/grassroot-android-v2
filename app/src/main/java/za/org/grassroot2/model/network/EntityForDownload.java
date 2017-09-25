package za.org.grassroot2.model.network;

import java.util.UUID;

/**
 * Created by luke on 2017/08/17.
 */

public interface EntityForDownload extends NetworkEntity {

    UUID getUid();
    long getLastTimeChangedServer();

}
