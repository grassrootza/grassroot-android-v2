package za.org.grassroot.android.services;

import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import timber.log.Timber;
import za.org.grassroot.android.model.UserProfile;
import za.org.grassroot.android.model.exception.RealmWriteOnMainThreadException;
import za.org.grassroot.android.model.network.EntityForDownload;

/**
 * Created by luke on 2017/08/10.
 * todo: figure out why there are constant warnings about closing. typical for an Android library,
 * manages to be thread unsafe, not allow objects across threads, suggest to keep instances open for
 * performance, but then also have memory leaks. and it's the least bad option.
 */

public class RealmServiceImpl implements RealmService {

    private Realm uiRealm;

    public RealmServiceImpl(final Realm realm) {
        this.uiRealm = realm;
    }

    @Override
    public void openUiRealm() {
        if (onMainThread() && uiRealm.isClosed()) {
            uiRealm = Realm.getDefaultInstance();
        }
    }

    // todo: watch all of this, and try switch to pattern of open one on first activity created and delete when all closed
    @Override
    public void closeUiRealm() {
        uiRealm.close();
    }

    @Override
    public void closeRealmOnThread() {
        Realm tRealm = Realm.getDefaultInstance();
        tRealm.close();
    }

    @Override
    public void wipeRealm() {
        uiRealm.deleteAll();
        uiRealm.close();
    }

    @Override
    public <E extends RealmObject> E loadObjectByUid(Class<E> clazz, String uid, boolean closeRealm) {
        if (onMainThread()) {
            if (uiRealm.isClosed()) {
                uiRealm = Realm.getDefaultInstance();
            }
            return uiRealm.where(clazz).equalTo("uid", uid).findFirst();
        } else {
            Realm threadR = Realm.getDefaultInstance();
            E object = threadR.copyFromRealm(threadR.where(clazz).equalTo("uid", uid).findFirst());
            if (closeRealm) {
                threadR.close();
            }
            return object;
        }
    }

    @Override
    public UserProfile loadUserProfile() {
        Realm tRealm = onMainThread() ? uiRealm : Realm.getDefaultInstance();
        return tRealm.where(UserProfile.class).equalTo("id", 0).findFirst();
    }

    @Override
    public <E extends RealmObject> Map<String, Long> loadExistingObjectsWithLastChangeTime(Class<E> clazz) {
        // this is slightly heavy so don't do it on the main thread
        validateOffMainThread();
        if (!EntityForDownload.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Error! Trying to load non-downloadable entity");
        }
        Map<String, Long> returnMap = new HashMap<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<E> results = realm
                .where(clazz)
                .findAll();
        EntityForDownload entityHolder;
        for (int i = 0; i < results.size(); i++) {
            entityHolder = (EntityForDownload) results.get(i);
            returnMap.put(entityHolder.getUid(), entityHolder.getLastTimeChangedServer());
        }
        realm.close();
        Timber.e("return map = " + returnMap);
        return returnMap;
    }

    @Override
    public <E extends RealmObject> E storeRealmObject(E object, boolean closeRealm) {
        validateOffMainThread();
        return copyObjectToRealmAndReturnUid(Realm.getDefaultInstance(), object, true);
    }

    // in the rest, we open and close these ourselves
    @Override
    public UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone, final String userDisplayName, final String userSystemRole) {
        validateOffMainThread();
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(UserProfile.class).count() == 0) {
            final UserProfile newUserProfile = new UserProfile(userUid, userPhone, userDisplayName, userSystemRole);
            safeRealmTransaction(realm, new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(newUserProfile);
                }
            });
            return newUserProfile;
        } else {
            final UserProfile existingUserProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
            safeRealmTransaction(realm, new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    existingUserProfile.updateFields(userUid, userPhone, userDisplayName, userSystemRole);
                }
            });
            return existingUserProfile;
        }
    }


    @Override
    public void removeUserProfile() {
        validateOffMainThread();
        Realm realm = Realm.getDefaultInstance();
        final UserProfile userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
        if (userProfile != null) {
            safeRealmTransaction(realm, new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    userProfile.deleteFromRealm();
                }
            });
        }
    }

    private boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void validateOffMainThread() {
        if (onMainThread()) {
            throw new RealmWriteOnMainThreadException();
        }
    }

    private void safeRealmTransaction(Realm realm, Realm.Transaction transaction) {
        try {
            realm.executeTransaction(transaction);
        } finally {
            realm.close();
        }
    }

    // don't use this because Realm bizarrely says use primary key but then enforces it as param, which is just bizarre
    private <E extends RealmObject> E realmObjectCreate(Realm threadR, Class<E> clazz, boolean closeRealm) {
        E object = null;
        try {
            threadR.beginTransaction();
            object = threadR.createObject(clazz, 0);
            threadR.commitTransaction();
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (closeRealm) {
                if (object != null) {
                    object = threadR.copyFromRealm(object);
                }
                threadR.close();
            }
        }
        return object;
    }

    private <E extends RealmObject> E copyObjectToRealmAndReturnUid(Realm threadR, E object, boolean closeRealm) {
        E storedObject = null;
        try {
            threadR.beginTransaction();
            storedObject = threadR.copyToRealmOrUpdate(object);
            threadR.commitTransaction();
        } finally {
            if (closeRealm) {
                if (storedObject != null) {
                    storedObject = threadR.copyFromRealm(storedObject);
                }
                threadR.close();
            }
        }
        return storedObject;
    }
}
