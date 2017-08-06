package za.org.grassroot.android.services.user;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import timber.log.Timber;
import za.org.grassroot.android.model.UserProfile;

/**
 * TODO: insert into dependency injection instead of nasty static methods
 */
public class UserDetailsService {

    public static Single<UserProfile> storeUserDetails(final String userUid,
                                                final String userPhone,
                                                final String userDisplayName,
                                                final String userSystemRole) {
        return Single.create(new SingleOnSubscribe<UserProfile>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<UserProfile> e) throws Exception {
                Realm realm = Realm.getDefaultInstance();
                UserProfile userProfile;
                if (realm.where(UserProfile.class).count() == 0) {
                    userProfile = new UserProfile();
                } else {
                    userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
                }
                realm.beginTransaction();
                userProfile.updateFields(userUid, userPhone, userDisplayName, userSystemRole);
                realm.commitTransaction();
                e.onSuccess(userProfile);
            }
        });
    }

    public static String getUserUid() {
        Timber.d("trying to get user Uid");
        Realm realm = Realm.getDefaultInstance();
        UserProfile userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
        Timber.d("found user profile: " + userProfile);
        final String uid = userProfile == null ? null : userProfile.getUid();
        realm.close();
        return uid;
    }
}
