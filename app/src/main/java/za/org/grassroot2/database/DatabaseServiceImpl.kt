package za.org.grassroot2.database

import android.util.Log
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import za.org.grassroot2.model.*
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.model.network.Syncable
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class DatabaseServiceImpl(private val helper: DatabaseHelper) : DatabaseService {

    override fun getMeetings(taskUid: String): Maybe<List<Post>> {
        return Maybe.create { e ->
            try {
                val dao = helper.getDao(Post::class.javaObjectType)
                val result = dao.queryBuilder().where().eq("meeting_id", taskUid).query()
                e.onSuccess(result)
                e.onComplete()
            } catch (ex: SQLException) {
                ex.printStackTrace()
                e.onError(ex)
            }
        }
    }

    override fun storePosts(meeting: Meeting, posts: List<Post>) {
        try {
            val dao = helper.getDao(Post::class.javaObjectType)
            posts.forEach {
                it.meeting = meeting
                dao.createOrUpdate(it) }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun wipeDatabase() {
        helper.clearDatabase()
    }

    override fun loadUserProfile(): UserProfile? {
        try {
            val dao = helper.getDao(UserProfile::class.javaObjectType)
            return dao.queryBuilder().queryForFirst()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return null
    }

    override fun <E> load(clazz: Class<E>, uid: String): Maybe<E> {
        return Maybe.create { e ->
            try {
                val dao = helper.getDao(clazz)
                val result = dao.queryBuilder().where().eq("uid", uid).queryForFirst()
                if (result != null) {
                    e.onSuccess(result)
                }
                e.onComplete()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }

    override fun <E> loadObjectByUid(cls: Class<E>, uid: String): E? {
        try {
            val dao = helper.getDao(cls)
            val data = dao.queryBuilder().where().eq("uid", uid).queryForFirst()
            dao.refresh(data)
            return data
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        return null
    }

    override fun loadGroup(uid: String): Group? {
        try {
            val dao = helper.getDao(Group::class.javaObjectType)
            return dao.queryBuilder().where().eq("uid", uid).queryForFirst()
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        return null
    }

    override fun <E> loadExistingObjectsWithLastChangeTime(clazz: Class<E>): Map<String, Long> {
        val returnMap = HashMap<String, Long>()
        try {
            val dao = helper.getDao(clazz)
            val result = dao.queryBuilder().query()
            var entityHolder: EntityForDownload
            for (row in result) {
                entityHolder = row as EntityForDownload
                returnMap.put(entityHolder.uid, entityHolder.lastTimeChangedServer)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return returnMap
    }

    override fun getTasksLastChangedTimestamp(groupUid: String): Map<String, Long> {
        val returnMap = HashMap<String, Long>()
        var entityHolder: EntityForDownload
        val tasks = ArrayList<Task>()
        tasks.addAll(loadObjectsByParentUid(Meeting::class.javaObjectType, groupUid))
        tasks.addAll(loadObjectsByParentUid(Vote::class.javaObjectType, groupUid))
        tasks.addAll(loadObjectsByParentUid(Todo::class.javaObjectType, groupUid))
        for (row in tasks) {
            entityHolder = row
            returnMap.put(entityHolder.uid, entityHolder.lastTimeChangedServer)
        }
        return returnMap
    }

    override fun <E> loadObjects(clazz: Class<E>): List<E> {
        val returnList = ArrayList<E>()
        try {
            val dao = helper.getDao(clazz)
            val result = dao.queryBuilder().query()
            returnList.addAll(result)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return returnList
    }

    override fun <E> load(clazz: Class<E>): Maybe<List<E>> {
        return Maybe.create { e ->
            val returnList = ArrayList<E>()
            try {
                val dao = helper.getDao(clazz)
                val result = dao.queryBuilder().query()
                returnList.addAll(result)
                e.onSuccess(returnList)
                e.onComplete()
            } catch (ex: SQLException) {
                ex.printStackTrace()
                e.onError(ex)
            }
        }
    }

    override fun loadPublicMeetings(): Maybe<List<AroundEntity>> {
        return Maybe.create { e ->
            val returnList = ArrayList<AroundEntity>()
            try {
                val dao = helper.getDao(AroundEntity::class.javaObjectType)
                val result = dao.queryBuilder().where().eq("type", GrassrootEntityType.MEETING).query()
                returnList.addAll(result)
                e.onSuccess(returnList)
                e.onComplete()
            } catch (ex: SQLException) {
                ex.printStackTrace()
                e.onError(ex)
            }
        }
    }

    override fun <E> loadObjectsSortedByDate(clazz: Class<E>): List<E> {
        val returnList = ArrayList<E>()
        try {
            val dao = helper.getDao(clazz)
            val builder = dao.queryBuilder()
            val result = builder.orderBy("deadlineMillis", false).query()
            returnList.addAll(result)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return returnList
    }

    override fun loadGroupsSorted(): List<Group> {
        val returnList = ArrayList<Group>()
        try {
            val dao = helper.getDao(Group::class.javaObjectType)
            val result = dao.queryBuilder()
                    .orderBy("lastTimeChangedServer", false)
                    .where().eq("hidden", false)
                    .query()
            returnList.addAll(result)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return returnList
    }

    override fun loadMembersForGroup(groupUid: String): Single<List<Membership>> {
        return Single.create { e->
            val returnList = ArrayList<Membership>()
            try {
                // doing it this way instead of group and get members to avoid handling iterator etc
                // also note: we don't use order by because we resort programmatically to deal with phone number names
                val dao = helper.getDao(Membership::class.javaObjectType)
                val result = dao.queryBuilder()
                        .where().eq("groupUid", groupUid)
                        .query()
                returnList.addAll(result)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            e.onSuccess(returnList)
        }
    }


    override fun <E> loadObjectsByName(clazz: Class<E>, nameQuery: String): List<E> {
        val query = StringBuilder().append("%").append(nameQuery).append("%")
        val returnList = ArrayList<E>()
        try {
            val dao = helper.getDao(clazz)
            val result = dao.queryBuilder().where().like("name", query.toString()).query()
            returnList.addAll(result)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return returnList
    }

    override fun loadTasksForGroup(groupUid: String, type: GrassrootEntityType?): Single<List<Task>> {
        return Single.create { e ->
            val returnList = ArrayList<Task>()
            if (type == null) {
                returnList.addAll(loadObjectsByParentUid(Meeting::class.javaObjectType, groupUid))
                returnList.addAll(loadObjectsByParentUid(Vote::class.javaObjectType, groupUid))
                returnList.addAll(loadObjectsByParentUid(Todo::class.javaObjectType, groupUid))
            } else {
                when (type) {
                    GrassrootEntityType.MEETING -> returnList.addAll(loadObjectsByParentUid(Meeting::class.javaObjectType, groupUid))
                    GrassrootEntityType.VOTE -> returnList.addAll(loadObjectsByParentUid(Vote::class.javaObjectType, groupUid))
                    GrassrootEntityType.TODO -> returnList.addAll(loadObjectsByParentUid(Todo::class.javaObjectType, groupUid))
                }
            }
            e.onSuccess(returnList)
        }
    }

    override fun loadAllTasksSorted(): Maybe<List<Task>> {
        return Maybe.create({e ->
            val tmpList = ArrayList<Task>()
            tmpList.addAll(loadObjectsSortedByDate(Meeting::class.javaObjectType))
            tmpList.addAll(loadObjectsSortedByDate(Vote::class.javaObjectType))
            tmpList.addAll(loadObjectsSortedByDate(Todo::class.javaObjectType))
            tmpList.sortByDescending { task -> task.deadlineMillis }
            e.onSuccess(tmpList)
            e.onComplete()
        })
        
    }

    fun <E> loadObjectsByParentUid(cls: Class<E>, parentUid: String): List<E> {
        var result: List<E> = ArrayList()
        try {
            val taskDao = helper.getDao(cls)
            result = taskDao.queryBuilder().where().eq("parentUid", parentUid).query()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return result
    }

    override fun <E> store(cls: Class<E>, `object`: E): Single<E> {
        return Single.create { e ->
            try {
                val dao = helper.getDao(cls)
                dao.createOrUpdate(`object`)
            } catch (ex: SQLException) {
                Timber.e("Error while saving object: %s", `object`.toString())
                ex.printStackTrace()
            }

            e.onSuccess(`object`)
        }
    }

    override fun <E> storeObject(cls: Class<E>, `object`: E): E {
        try {
            val dao = helper.getDao(cls)
            dao.createOrUpdate(`object`)
        } catch (ex: SQLException) {
            Log.e(TAG, "Error while saving object: " + `object`.toString())
            ex.printStackTrace()
        }

        return `object`
    }

    override fun <E> copyOrUpdateListOfEntities(cls: Class<E>, objects: List<E>): List<E> {
        if (!objects.isEmpty()) {
            try {
                val dao = helper.getDao(cls)
                for (item in objects) {
                    dao.createOrUpdate(item)
                }
            } catch (e: SQLException) {
                Log.e(TAG, "Error while saving list of: " + objects.toString())
                e.printStackTrace()
            }

        }
        return objects
    }

    override fun updateOrCreateUserProfile(userUid: String, userPhone: String, userDisplayName: String, email: String?, languageCode: String, userSystemRole: String?): UserProfile {
        val newProfile = UserProfile(userUid, userPhone, userDisplayName, email, languageCode, userSystemRole)
        try {
            val dao = helper.getDao(UserProfile::class.java)
            dao.createOrUpdate(newProfile)
        } catch (e: SQLException) {
            Timber.e("Error while saving user profile: " + newProfile.toString())
            e.printStackTrace()
        }

        return newProfile
    }

    override fun removeUserProfile() {
        try {
            val dao = helper.getDao(UserProfile::class.javaObjectType)
            dao.deleteBuilder().delete()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun storeGroupWithMembers(group: Group): Single<Group> {
        return Single.create { e ->
            try {
                // todo : ORMLite foreign collection docs not the greatest, so likely a better way to do this, but for now
                val groupDao = helper.getDao(Group::class.java)
                groupDao.createOrUpdate(group)
                val memberDao = helper.getDao(Membership::class.java)
                memberDao.callBatchTasks({
                    group.memberships.forEach { memberDao.createOrUpdate(it) }
                })
            } catch (ex: SQLException) {
                Timber.e("Error while saving group %s", group.toString())
            }
            e.onSuccess(group)
        }
    }

    override fun removeGroup(groupUid: String): Boolean {
        try {
            val dao = helper.getDao(Group::class.javaObjectType)
            val deleteBuilder = dao.deleteBuilder()
            deleteBuilder.where().eq("uid", groupUid)
            deleteBuilder.delete()
            return true
        } catch (ex: SQLException) {
            Timber.e("Error while deleting group with UID: " + groupUid)
            ex.printStackTrace()
            return false
        }
    }

    override fun <E> listAllEntitesOfType(clazz: Class<E>) {
        Timber.d("listing entities ...")
        try {
            val dao = helper.getDao(clazz)
            val result = dao.queryBuilder().query()
            for (obj in result) {
                Timber.v("entity: " + obj)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    override fun storeMembersInvites(requests: List<MemberRequest>) {
        for (r in requests) {
            storeObject(MemberRequest::class.javaObjectType, r)
        }
    }

    override fun getMemberRequestsToSync(): Observable<List<MemberRequest>> {
        return Observable.fromCallable {
            val returnList = ArrayList<MemberRequest>()
            try {
                val dao = helper.getDao(MemberRequest::class.javaObjectType)
                val result = dao.queryBuilder().orderBy("createdDate", true).query()
                returnList.addAll(result)
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            returnList
        }
    }

    override fun <E : Syncable> getObjectsToSync(cls: Class<E>): Observable<List<E>> {
        return Observable.fromCallable {
            val returnList = ArrayList<E>()
            try {
                val dao = helper.getDao(cls)
                val meetingQueryBuilder = dao.queryBuilder()
                meetingQueryBuilder.where().eq("synced", false)
                val result = meetingQueryBuilder.orderBy("createdDate", true).query()
                returnList.addAll(result)
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            returnList
        }
    }

    override fun storeTasks(data: List<Task>) {
        for (t in data) {
            val type = t.type
            when (type) {
                GrassrootEntityType.VOTE -> storeObject(Vote::class.javaObjectType, t as Vote)
                GrassrootEntityType.MEETING -> storeObject(Meeting::class.javaObjectType, t as Meeting)
                GrassrootEntityType.TODO -> storeObject(Todo::class.javaObjectType, t as Todo)
                else -> {
                }
            }
        }
    }

    override fun getAllTasksLastChangedTimestamp(): Map<String, Long> {
        val returnMap = HashMap<String, Long>()
        var entityHolder: EntityForDownload
        val tasks = ArrayList<Task>()
        tasks.addAll(loadObjects(Meeting::class.javaObjectType))
        tasks.addAll(loadObjects(Vote::class.javaObjectType))
        tasks.addAll(loadObjects(Todo::class.javaObjectType))
        for (row in tasks) {
            entityHolder = row
            returnMap.put(entityHolder.uid, entityHolder.lastTimeChangedServer)
        }
        return returnMap
    }

    override fun delete(r: MemberRequest) {
        try {
            val dao = helper.getDao(MemberRequest::class.javaObjectType)
            dao.delete(r)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    override fun <E> deleteAll(cls: Class<E>) {
        try {
            val dao = helper.getDao(cls)
            dao.deleteBuilder().delete()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun <E> delete(cls: Class<E>, item: E) {
        try {
            val dao = helper.getDao(cls)
            dao.delete(item)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    companion object {

        private val TAG = DatabaseServiceImpl::class.javaObjectType.simpleName
    }
}
