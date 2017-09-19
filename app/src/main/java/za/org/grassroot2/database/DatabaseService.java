package za.org.grassroot2.database;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.UserProfile;

/**
 * Created by luke on 2017/08/10.
 */

public interface DatabaseService {

    void wipeDatabase();
    // read methods
    UserProfile loadUserProfile();

    <E> Single<E> load(Class<E> clazz, String uid);
    <E> E loadObjectByUid(Class<E> cls, String uid);
    Group loadGroup(String uid);

    <E> Map<String, Long> loadExistingObjectsWithLastChangeTime(Class<E> clazz);

    <E> List<E> loadObjectsForSelection(Class<E> clazz);


    // write and read methods (can only be called within an observable on background thread)
    <E> Single<E> store(Class<E> cls, E object);
    <E> E storeObject(Class<E> cls, E object);
    <E> List<E> copyOrUpdateListOfEntities(Class<E> cls, List<E> objects);

//    void executeTransaction(Realm.Transaction transaction);
    UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone,
                                          final String userDisplayName, final String userSystemRole);

    void removeUserProfile();

    // only for debugging
    <E> void listAllEntitesOfType(Class<E> clazz);
}
