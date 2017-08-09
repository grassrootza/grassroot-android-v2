package za.org.grassroot.android.services.auth;

import io.reactivex.Single;
import za.org.grassroot.android.model.UserProfile;

/**
 * Created by luke on 2017/08/09.
 */

public interface UserDetailsService {

    Single<UserProfile> storeUserDetails(final String userUid, final String userPhone,
                                         final String userDisplayName, final String userSystemRole);

    Single<Boolean> logoutRetainingData();
    Single<Boolean> logoutWipingData();

    String getCurrentToken();
    String getCurrentUserUid();
    String getCurrentUserMsisdn();

}