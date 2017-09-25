package za.org.grassroot2.database;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import timber.log.Timber;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.model.network.EntityForDownload;

/**
 * Created by qbasso on 18.09.2017.
 */

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
                e.onSuccess(dao.queryBuilder().where().eq("uid", UUID.fromString(uid)).queryForFirst());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public <E> E loadObjectByUid(Class<E> cls, String uid) {
        try {
            Dao<E, ?> dao = helper.getDao(cls);
            return dao.queryBuilder().where().eq("uid", UUID.fromString(uid)).queryForFirst();
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
                returnMap.put(entityHolder.getUid().toString(), entityHolder.getLastTimeChangedServer());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    @Override
    public <E> List<E> loadObjectsForSelection(Class<E> clazz) {
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
    public <E> Single<E> store(final Class<E> cls, final E object) {
        return Single.create(new SingleOnSubscribe<E>() {
            @Override
            public void subscribe(SingleEmitter<E> e) throws Exception {
                try {
                    Dao<E, ?> dao = helper.getDao(cls);
                    dao.createOrUpdate(object);
                } catch (SQLException ex) {
                    Log.e(TAG, "Error while saving object: " + object.toString());
                    ex.printStackTrace();
                }
                e.onSuccess(object);
            }
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
}
