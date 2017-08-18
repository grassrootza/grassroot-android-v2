package za.org.grassroot.android.services;

import java.util.Map;

import io.realm.RealmObject;
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
    <E extends RealmObject> E loadObjectByUid(Class<E> clazz, String uid, boolean closeRealm);
    UserProfile loadUserProfile();
    <E extends RealmObject> Map<String, Long> loadExistingObjectsWithLastChangeTime(Class<E> clazz);

    // write and read methods (can only be called within an observable on background thread)

    /**
     *
     * @param <E> The class of the object to store
     * @param object The object stored
     * @param closeRealm
     * @return The uid of the created stored object
     */
    <E extends RealmObject> E storeRealmObject(E object, boolean closeRealm);
    UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone,
                                          final String userDisplayName, final String userSystemRole);
    void removeUserProfile();

}
