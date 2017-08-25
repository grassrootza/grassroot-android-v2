package za.org.grassroot.android.services;

import android.os.Looper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import timber.log.Timber;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.model.UserProfile;
import za.org.grassroot.android.model.exception.RealmReadOffMainThreadException;
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

    private Realm getRealm() {
        if (!onMainThread()) {
            return Realm.getDefaultInstance();
        } else {
            if (uiRealm.isClosed()) {
                uiRealm = Realm.getDefaultInstance();
            }
            return uiRealm;
        }
    }

    private void closeRealm(Realm realm) {
        if (!onMainThread()) {
            realm.close();
        }
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
            E managedObject = threadR.where(clazz).equalTo("uid", uid).findFirst();
            E returnObject = managedObject != null ? threadR.copyFromRealm(managedObject) : null;
            if (closeRealm) {
                threadR.close();
            }
            return returnObject;
        }
    }

    @Override
    public UserProfile loadUserProfile() {
        Realm tRealm = onMainThread() ? uiRealm : Realm.getDefaultInstance();
        return tRealm.where(UserProfile.class).equalTo("id", 0).findFirst();
    }

    @Override
    public <E extends RealmObject> Single<E> load(final Class<E> clazz, final String uid) {
        return Single.create(new SingleOnSubscribe<E>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<E> e) throws Exception {
                Realm tRealm = getRealm();
                try {
                    E managedObject = tRealm.where(clazz).equalTo("uid", uid).findFirst();
                    e.onSuccess(onMainThread() ? managedObject :
                        managedObject == null ? null : tRealm.copyFromRealm(managedObject));
                } finally {
                    closeRealm(tRealm);
                }
            }
        });
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
    public <E extends RealmObject & SelectableItem> RealmResults<E> loadObjectsForSelection(Class<E> clazz) {
        validateOnMainThread();
        checkUiRealmOpen();
        return uiRealm.where(clazz).findAll();
    }

    @Override
    public <E extends RealmObject> Single<E> store(final E object) {
        return Single.create(new SingleOnSubscribe<E>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<E> e) throws Exception {
                e.onSuccess(copyObjectToRealmAndReturn(Realm.getDefaultInstance(),
                        object, true));
            }
        });
    }

    @Override
    public <E extends RealmObject> E storeRealmObject(E object, boolean closeRealm) {
        validateOffMainThread();
        return copyObjectToRealmAndReturn(Realm.getDefaultInstance(), object, closeRealm);
    }

    public <E extends RealmObject> void copyOrUpdateListOfEntities(final List<E> objects) {
        validateOffMainThread();
        safeRealmTransaction(Realm.getDefaultInstance(), new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(objects);
            }
        });
    }

    // todo: watch out for returning the wrong entity--overall, clean this up / rethink it
    @Override
    public UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone, final String userDisplayName, final String userSystemRole) {
        validateOffMainThread();
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(UserProfile.class).count() == 0) {
            return createNewProfile(realm, userUid, userPhone, userDisplayName, userSystemRole);
        } else {
            final UserProfile existingUserProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
            try {
                safeRealmTransaction(realm, new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        existingUserProfile.updateFields(userUid, userPhone, userDisplayName, userSystemRole);
                    }
                });
                return existingUserProfile;
            } catch (IllegalStateException e) {
                // because, Realm; because, Android
                return createNewProfile(realm, userUid, userPhone, userDisplayName, userSystemRole);
            }
        }
    }

    private UserProfile createNewProfile(Realm realm, final String userUid, final String userPhone, final String userDisplayName, final String userSystemRole) {
        final UserProfile newUserProfile = new UserProfile(userUid, userPhone, userDisplayName, userSystemRole);
        safeRealmTransaction(realm, new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(newUserProfile);
            }
        });
        return newUserProfile;
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

    private void validateOnMainThread() {
        if (!onMainThread()) {
            throw new RealmReadOffMainThreadException();
        }
    }

    private void validateOffMainThread() {
        if (onMainThread()) {
            throw new RealmWriteOnMainThreadException();
        }
    }

    private void checkUiRealmOpen() {
        if (onMainThread() && uiRealm == null || uiRealm.isClosed()) {
            Timber.d("on main thread and UI realm was closed...");
            uiRealm = Realm.getDefaultInstance();
        }
    }

    private void safeRealmTransaction(Realm realm, Realm.Transaction transaction) {
        try {
            realm.executeTransaction(transaction);
        } finally {
            if (realm != null) {
                realm.close();
            }
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
            if (closeRealm && threadR != null) {
                if (object != null) {
                    object = threadR.copyFromRealm(object);
                }
                threadR.close();
            }
        }
        return object;
    }

    private <E extends RealmObject> E copyObjectToRealmAndReturn(Realm threadR, E object, boolean closeRealm) {
        E storedObject = null;
        try {
            threadR.beginTransaction();
            storedObject = threadR.copyToRealmOrUpdate(object);
            threadR.commitTransaction();
        } finally {
            if (closeRealm && threadR != null) {
                if (storedObject != null) {
                    storedObject = threadR.copyFromRealm(storedObject);
                }
                threadR.close();
            }
        }
        Timber.e("is stored object null? " + (storedObject == null));
        return storedObject;
    }

    // for debugging, because Android, because Realm -- you would think telling it to store something would guaranteed store something, but no
    public void listAllEntitesOfType(Class clazz) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults results = realm.where(clazz).findAll();
        for (int i = 0; i < results.size(); i++) {
            Timber.d("entity: " + results.get(i));
        }
        realm.close();
    }
}
