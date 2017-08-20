package za.org.grassroot.android.services;

import java.util.List;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.RealmResults;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.model.UserProfile;

/**
 * Created by luke on 2017/08/10.
 */

public interface RealmService {

    void openUiRealm();
    void closeUiRealm();
    void closeRealmOnThread();
    void wipeRealm();

    // read methods
    UserProfile loadUserProfile();

    <E extends RealmObject> E loadObjectByUid(Class<E> clazz, String uid, boolean closeRealm);
    <E extends RealmObject> Map<String, Long> loadExistingObjectsWithLastChangeTime(Class<E> clazz);

    <E extends RealmObject & SelectableItem> RealmResults<E> loadObjectsForSelection(Class<E> clazz);

    // write and read methods (can only be called within an observable on background thread)

    /**
     *
     * @param <E> The class of the object to store
     * @param object The object stored
     * @param closeRealm
     * @return The uid of the created stored object
     */
    <E extends RealmObject> E storeRealmObject(E object, boolean closeRealm);
    <E extends RealmObject> void copyOrUpdateListOfEntities(List<E> objects);

    UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone,
                                          final String userDisplayName, final String userSystemRole);
    void removeUserProfile();

}
