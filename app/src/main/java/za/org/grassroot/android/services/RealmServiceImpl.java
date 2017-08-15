package za.org.grassroot.android.services;

import android.os.Looper;

import io.realm.Realm;
import io.realm.RealmObject;
import timber.log.Timber;
import za.org.grassroot.android.model.UserProfile;
import za.org.grassroot.android.model.exception.RealmWriteOnMainThreadException;

/**
 * Created by luke on 2017/08/10.
 */

public class RealmServiceImpl implements RealmService {

    private final Realm uiRealm;

    public RealmServiceImpl(final Realm realm) {
        this.uiRealm = realm;
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
        return uiRealm.where(UserProfile.class).equalTo("id", 0).findFirst();
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
        final UserProfile userProfile = realm.where(UserProfile.class).count() == 0 ?
                realmObjectCreate(realm, UserProfile.class, false) : realm.where(UserProfile.class).equalTo("id", 0).findFirst();
        safeRealmTransaction(realm, new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                userProfile.updateFields(userUid, userPhone, userDisplayName, userSystemRole);
            }
        });
        return userProfile;
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

    private <E extends RealmObject> E realmObjectCreate(Realm threadR, Class<E> clazz, boolean closeRealm) {
        E object = null;
        try {
            threadR.beginTransaction();
            object = threadR.createObject(clazz);
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
            storedObject = threadR.copyToRealm(object);
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
