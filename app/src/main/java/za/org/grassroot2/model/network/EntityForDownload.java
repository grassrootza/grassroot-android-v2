package za.org.grassroot2.model.network;

/**
 * Created by luke on 2017/08/17.
 */

public interface EntityForDownload extends NetworkEntity {

    String getUid();
    long getLastTimeChangedServer();

}
