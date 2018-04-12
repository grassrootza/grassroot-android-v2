package za.org.grassroot2.database

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import za.org.grassroot2.model.*
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.network.Syncable
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo

/**
 * Created by luke on 2017/08/10.
 */

interface DatabaseService {

    fun wipeDatabase()
    fun loadUserProfile(): UserProfile?

    fun <E> load(clazz: Class<E>, uid: String): Maybe<E>
    fun <E> loadObjectByUid(cls: Class<E>, uid: String): E?
    fun loadGroup(uid: String): Group?
    fun <E> loadExistingObjectsWithLastChangeTime(clazz: Class<E>): Map<String, Long>

    fun getTasksLastChangedTimestamp(groupUid: String): Map<String, Long>

    fun <E> loadObjects(clazz: Class<E>): List<E>

    fun loadGroupsSorted(): List<Group>

    fun <E> loadObjectsByName(clazz: Class<E>, nameQuery: String): List<E>

    fun loadTasksForGroup(groupUid: String, type: GrassrootEntityType?): Single<List<Task>>

    fun loadMembersForGroup(groupUid: String): Single<List<Membership>>
    fun loadMemberLogsForGroup(groupUid: String): Single<List<MembershipLog>>
    fun countMembersJoinedSince(groupUid: String, cutOffDate: Long): Long

    // write and read methods (can only be called within an observable on background thread)
    fun <E> store(cls: Class<E>, `object`: E): Single<E>


    fun <E> storeObject(cls: Class<E>, `object`: E): E
    fun <E> copyOrUpdateListOfEntities(cls: Class<E>, objects: List<E>): List<E>


    fun updateOrCreateUserProfile(userUid: String, userPhone: String, userDisplayName: String,
                                  email: String?, languageCode: String, userSystemRole: String?): UserProfile

    fun removeUserProfile()
    fun removeGroup(groupUid: String): Boolean

    fun storeGroupWithMembers(group: Group): Single<Group>

    // only for debugging
    fun <E> listAllEntitesOfType(clazz: Class<E>)

    fun storeMembersInvites(requests: List<MemberRequest>)

    fun <E : Syncable> getObjectsToSync(cls: Class<E>): Observable<List<E>>

    fun storeTasks(data: List<Task>)

    fun delete(r: MemberRequest)

    fun <E> delete(cls: Class<E>, item: E)
    fun <E> loadObjectsSortedByDate(clazz: Class<E>): List<E>
    fun getMemberRequestsToSync(): Observable<List<MemberRequest>>
    fun getAllTasksLastChangedTimestamp(): Map<String, Long>
    fun loadAllTasksSorted(): Maybe<List<Task>>
    fun <E> load(clazz: Class<E>): Maybe<List<E>>
    fun <E> deleteAll(cls: Class<E>)
    fun loadPublicMeetings(): Maybe<List<AroundEntity>>
    fun storePosts(meeting: Meeting, posts: List<Post>)
    fun getMeetings(taskUid: String): Maybe<List<Post>>
}
