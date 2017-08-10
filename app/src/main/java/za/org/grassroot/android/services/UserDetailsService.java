package za.org.grassroot.android.services;

import io.reactivex.Single;
import za.org.grassroot.android.model.UserProfile;
import za.org.grassroot.android.services.GrassrootService;

/**
 * Created by luke on 2017/08/09.
 */

public interface UserDetailsService extends GrassrootService {

    Single<UserProfile> storeUserDetails(final String userUid,
                                         final String userPhone,
                                         final String userDisplayName,
                                         final String userSystemRole,
                                         final String userToken);

    Single<Boolean> logoutRetainingData();
    Single<Boolean> logoutWipingData();

    String getCurrentToken();
    String getCurrentUserUid();
    String getCurrentUserMsisdn();

}