package za.org.grassroot.android.model.network;

import io.realm.RealmObject;

/**
 * Created by luke on 2017/08/17.
 */

public interface EntityForDownload extends NetworkEntity {

    long getLastTimeChangedServer();
    RealmObject getRealmObject();

}
