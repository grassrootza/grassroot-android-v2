package za.org.grassroot.android.services;

import io.realm.RealmObject;
import za.org.grassroot.android.model.UserProfile;

/**
 * Created by luke on 2017/08/10.
 */

public interface RealmService {

    void closeRealm();
    void wipeRealm();

    // read methods
    UserProfile loadUserProfile();

    // write and read methods (can only be called within an observable on background thread)
    UserProfile updateOrCreateUserProfile(final String userUid, final String userPhone,
                                          final String userDisplayName, final String userSystemRole);
    void removeUserProfile();

}
