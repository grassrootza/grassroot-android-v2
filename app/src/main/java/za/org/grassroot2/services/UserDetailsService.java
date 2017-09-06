package za.org.grassroot2.services;

import io.reactivex.Single;
import za.org.grassroot2.model.UserProfile;

/**
 * Created by luke on 2017/08/09.
 */

public interface UserDetailsService extends GrassrootService {

    Single<UserProfile> storeUserDetails(final String userUid,
                                         final String userPhone,
                                         final String userDisplayName,
                                         final String userSystemRole,
                                         final String userToken);

    Single<Boolean> logout(boolean deleteAndroidAccount, boolean wipeRealm);

    String getCurrentToken();
    String getCurrentUserUid();
    String getCurrentUserMsisdn();

    void requestSync();

}