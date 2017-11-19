package za.org.grassroot2.services;

import android.accounts.Account;

import io.reactivex.Single;
import za.org.grassroot2.model.UserProfile;

/**
 * Created by luke on 2017/08/09.
 */

public interface UserDetailsService extends GrassrootService {

    Single<UserProfile> storeUserDetails(final String userUid,
                                         final String userPhone,
                                         final String userDisplayName,
                                         final String userEmailAddress,
                                         final String userLanguageCode,
                                         final String userSystemRole,
                                         final String userToken);

    Account setAuthToken(String userToken);

    Single<Boolean> logout(boolean deleteAndroidAccount, boolean wipeRealm);

    boolean isSyncCompleted();

    boolean isSyncFailed();

    void setSyncState(int status);

    String getCurrentToken();
    String getCurrentUserUid();
    String getCurrentUserMsisdn();

    void requestSync();

}