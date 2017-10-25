package za.org.grassroot2.database;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.Task;

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

    Map<String, Long> getTasksLastChangedTimestamp(String groupUid);

    <E> List<E> loadObjects(Class<E> clazz);

    List<Group> loadGroupsSorted();

    <E> List<E> loadObjectsByName(Class<E> clazz, String nameQuery);

    Single<List<Task>> loadTasksForGroup(String groupUid, GrassrootEntityType type);

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

    void storeMembersInvites(List<MemberRequest> requests);

    Observable<List<MemberRequest>> getMemberRequestsToSync();

    <E extends Syncable> Observable<List<E>> getObjectsToSync(Class<E> cls);

    void storeTasks(List<Task> data);

    Map<String,Long> getAllTasksLastChangedTimestamp();

    void delete(MemberRequest r);

    <E> void delete(Class<E> cls, E item);
}
