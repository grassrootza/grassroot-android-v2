package za.org.grassroot2.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.lang.reflect.Member;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.model.network.Syncable;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.model.task.Todo;
import za.org.grassroot2.model.task.Vote;

public class DatabaseServiceImpl implements DatabaseService {

    private static final String TAG = DatabaseServiceImpl.class.getSimpleName();
    private final DatabaseHelper helper;

    public DatabaseServiceImpl(DatabaseHelper helper) {
        this.helper = helper;
    }

    @Override
    public void wipeDatabase() {
        helper.clearDatabase();
    }

    @Override
    public UserProfile loadUserProfile() {
        try {
            Dao<UserProfile, ?> dao = helper.getDao(UserProfile.class);
            return dao.queryBuilder().queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <E> Single<E> load(final Class<E> clazz, final String uid) {
        return Single.create(e -> {
            try {
                Dao<E, ?> dao = helper.getDao(clazz);
                e.onSuccess(dao.queryBuilder().where().eq("uid", uid).queryForFirst());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public <E> E loadObjectByUid(Class<E> cls, String uid) {
        try {
            Dao<E, ?> dao = helper.getDao(cls);
            return dao.queryBuilder().where().eq("uid", uid).queryForFirst();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Group loadGroup(String uid) {
        try {
            Dao<Group, ?> dao = helper.getDao(Group.class);
            return dao.queryBuilder().where().eq("uid", uid).queryForFirst();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public <E> Map<String, Long> loadExistingObjectsWithLastChangeTime(Class<E> clazz) {
        Map<String, Long> returnMap = new HashMap<>();
        try {
            Dao<E, ?> dao = helper.getDao(clazz);
            List<E> result = dao.queryBuilder().query();
            EntityForDownload entityHolder;
            for (E row : result) {
                entityHolder = (EntityForDownload) row;
                returnMap.put(entityHolder.getUid(), entityHolder.getLastTimeChangedServer());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    @Override
    public Map<String, Long> getTasksLastChangedTimestamp(String groupUid) {
        Map<String, Long> returnMap = new HashMap<>();
        EntityForDownload entityHolder;
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(loadObjectsByParentUid(Meeting.class, groupUid));
        tasks.addAll(loadObjectsByParentUid(Vote.class, groupUid));
        tasks.addAll(loadObjectsByParentUid(Todo.class, groupUid));
        for (Task row : tasks) {
            entityHolder = row;
            returnMap.put(entityHolder.getUid(), entityHolder.getLastTimeChangedServer());
        }
        return returnMap;
    }

    @Override
    public <E> List<E> loadObjects(Class<E> clazz) {
        List<E> returnList = new ArrayList<>();
        try {
            Dao<E, ?> dao = helper.getDao(clazz);
            List<E> result = dao.queryBuilder().query();
            returnList.addAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    @Override
    public List<Group> loadGroupsSorted() {
        List<Group> returnList = new ArrayList<>();
        try {
            Dao<Group, ?> dao = helper.getDao(Group.class);
            List<Group> result = dao.queryBuilder().orderBy("lastTimeChangedServer", false).query();
            returnList.addAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }


    @Override
    public <E> List<E> loadObjectsByName(Class<E> clazz, String nameQuery) {
        StringBuilder query = new StringBuilder().append("%").append(nameQuery).append("%");
        List<E> returnList = new ArrayList<>();
        try {
            Dao<E, ?> dao = helper.getDao(clazz);
            List<E> result = dao.queryBuilder().where().like("name", query.toString()).query();
            returnList.addAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    @Override
    public Single<List<Task>> loadTasksForGroup(String groupUid, GrassrootEntityType type) {
        return Single.create(e -> {
            List<Task> returnList = new ArrayList<>();
            if (type == null) {
                returnList.addAll(loadObjectsByParentUid(Meeting.class, groupUid));
                returnList.addAll(loadObjectsByParentUid(Vote.class, groupUid));
                returnList.addAll(loadObjectsByParentUid(Todo.class, groupUid));
            } else {
                switch (type) {
                    case MEETING:
                        returnList.addAll(loadObjectsByParentUid(Meeting.class, groupUid));
                        break;
                    case VOTE:
                        returnList.addAll(loadObjectsByParentUid(Vote.class, groupUid));
                        break;
                    case TODO:
                        returnList.addAll(loadObjectsByParentUid(Todo.class, groupUid));
                        break;
                }
            }
            e.onSuccess(returnList);
        });
    }

    public <E> List<E> loadObjectsByParentUid(Class<E> cls, String parentUid) {
        List<E> result = new ArrayList<>();
        try {
            Dao<E, ?> taskDao = helper.getDao(cls);
            result = taskDao.queryBuilder().where().eq("parentUid", parentUid).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public <E> Single<E> store(final Class<E> cls, final E object) {
        return Single.create(e -> {
            try {
                Dao<E, ?> dao = helper.getDao(cls);
                dao.createOrUpdate(object);
            } catch (SQLException ex) {
                Log.e(TAG, "Error while saving object: " + object.toString());
                ex.printStackTrace();
            }
            e.onSuccess(object);
        });
    }

    @Override
    public <E> E storeObject(final Class<E> cls, E object) {
        try {
            Dao<E, ?> dao = helper.getDao(cls);
            dao.createOrUpdate(object);
        } catch (SQLException ex) {
            Log.e(TAG, "Error while saving object: " + object.toString());
            ex.printStackTrace();
        }
        return object;
    }

    @Override
    public <E> List<E> copyOrUpdateListOfEntities(final Class<E> cls, List<E> objects) {
        if (!objects.isEmpty()) {
            try {
                Dao<E, ?> dao = helper.getDao(cls);
                for (E item : objects) {
                    dao.createOrUpdate(item);
                }
            } catch (SQLException e) {
                Log.e(TAG, "Error while saving list of: " + objects.toString());
                e.printStackTrace();
            }
        }
        return objects;
    }

    @Override
    public UserProfile updateOrCreateUserProfile(String userUid, String userPhone, String userDisplayName, String userSystemRole) {
        UserProfile newProfile = new UserProfile(userUid, userPhone, userDisplayName, userSystemRole);
        try {
            Dao<UserProfile, ?> dao = helper.getDao(UserProfile.class);
            dao.createOrUpdate(newProfile);
        } catch (SQLException e) {
            Log.e(TAG, "Error while saving user profile: " + newProfile.toString());
            e.printStackTrace();
        }
        return newProfile;
    }

    @Override
    public void removeUserProfile() {
        try {
            Dao<UserProfile, ?> dao = helper.getDao(UserProfile.class);
            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <E> void listAllEntitesOfType(Class<E> clazz) {
        Timber.d("listing entities ...");
        try {
            Dao<?, ?> dao = helper.getDao(clazz);
            List<?> result = dao.queryBuilder().query();
            for (Object obj : result) {
                Timber.v("entity: " + obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void storeMembersInvites(List<MemberRequest> requests) {
        for (MemberRequest r : requests) {
            storeObject(MemberRequest.class, r);
        }
    }

    @Override
    public Observable<List<Syncable>> getMemberRequestsToSync() {
        return Observable.fromCallable(() -> {
            List<Syncable> returnList = new ArrayList<>();
            try {
                Dao<MemberRequest, ?> dao = helper.getDao(MemberRequest.class);
                List<MemberRequest> result = dao.queryBuilder().orderBy("createdDate", true).query();
                returnList.addAll(result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return returnList;
        });
    }

    @Override
    public Observable<List<Syncable>> getMeetingsToSync() {
        return Observable.fromCallable(() -> {
            List<Syncable> returnList = new ArrayList<>();
            try {
                Dao<Meeting, ?> dao = helper.getDao(Meeting.class);
                QueryBuilder<Meeting, ?> meetingQueryBuilder = dao.queryBuilder();
                meetingQueryBuilder.where().eq("synced", false);
                List<Meeting> result = meetingQueryBuilder.orderBy("createdDateTimeMillis", true).query();
                returnList.addAll(result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return returnList;
        });
    }

    @Override
    public void storeTasks(List<Task> data) {
        for (Task t : data) {
            GrassrootEntityType type = t.getType();
            switch (type) {
                case VOTE:
                    storeObject(Vote.class, (Vote) t);
                    break;
                case MEETING:
                    storeObject(Meeting.class, (Meeting) t);
                    break;
                case TODO:
                    storeObject(Todo.class, (Todo) t);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Map<String, Long> getAllTasksLastChangedTimestamp() {
        Map<String, Long> returnMap = new HashMap<>();
        EntityForDownload entityHolder;
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(loadObjects(Meeting.class));
        tasks.addAll(loadObjects(Vote.class));
        tasks.addAll(loadObjects(Todo.class));
        for (Task row : tasks) {
            entityHolder = row;
            returnMap.put(entityHolder.getUid(), entityHolder.getLastTimeChangedServer());
        }
        return returnMap;
    }

    @Override
    public void delete(MemberRequest r) {
        try {
            Dao<MemberRequest, ?> dao = helper.getDao(MemberRequest.class);
            dao.delete(r);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <E> void delete(Class<E> cls, E item) {
        try {
            Dao<E, ?> dao = helper.getDao(cls);
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
