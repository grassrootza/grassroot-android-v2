package za.org.grassroot.android.services;

import android.os.Looper;

import io.realm.Realm;
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
    public void closeRealm() {
        uiRealm.close();
    }

    @Override
    public void wipeRealm() {
        uiRealm.deleteAll();
        uiRealm.close();
    }

    @Override
    public UserProfile loadUserProfile() {
        return uiRealm.where(UserProfile.class).equalTo("id", 0).findFirst();
    }

    // in the rest, we open and close these ourselves
    @Override
    public UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone, final String userDisplayName, final String userSystemRole) {
        validateOffMainThread();
        Realm realm = Realm.getDefaultInstance();
        final UserProfile userProfile = realm.where(UserProfile.class).count() == 0 ?
                new UserProfile() : realm.where(UserProfile.class).equalTo("id", 0).findFirst();
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

    private void validateOffMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
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
}
