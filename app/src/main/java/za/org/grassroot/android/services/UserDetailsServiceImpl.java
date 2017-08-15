package za.org.grassroot.android.services;

import android.accounts.Account;
import android.accounts.AccountManager;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot.android.model.UserProfile;
import za.org.grassroot.android.services.auth.AuthConstants;

public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountManager accountManager;
    private final RealmService realmService;

    @Inject
    public UserDetailsServiceImpl(AccountManager accountManager,
                                  RealmService realmService) {
        this.accountManager = accountManager;
        this.realmService = realmService;
    }

    @Override
    public void cleanUpForActivity() {
        realmService.closeUiRealm();
    }

    public Single<UserProfile> storeUserDetails(final String userUid,
                                                final String userPhone,
                                                final String userDisplayName,
                                                final String userSystemRole,
                                                final String userToken) {
        return Single.create(new SingleOnSubscribe<UserProfile>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<UserProfile> e) throws Exception {
                final Account account = getOrCreateAccount();
                accountManager.setAuthToken(account, AuthConstants.AUTH_TOKENTYPE, userToken);
                Timber.v("stored auth token, number accounts = " + accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE).length);
                UserProfile userProfile = realmService.updateOrCreateUserProfile(userUid, userPhone, userDisplayName, userSystemRole);
                e.onSuccess(userProfile);
            }
        });
    }

    // todo: add exception handling, also calls to server, GCM, etc
    @Override
    public Single<Boolean> logoutRetainingData() {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                // first, wipe the details stored in account
                Account account = getAccount();
                if (account != null) {
                    accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE,
                            accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE));
                    accountManager.setPassword(account, null);
                }
                realmService.removeUserProfile(); // then, wipe the UID etc
                e.onSuccess(true);
            }
        });
    }

    @Override
    public Single<Boolean> logoutWipingData() {
        return logoutRetainingData()
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Boolean aBoolean) throws Exception {
                        // need to think a bit more about this opening & closing of realms
                        realmService.wipeRealm();
                        return true;
                    }
                });
    }

    public String getCurrentToken() {
        Account account = getAccount();
        return account == null ? null : accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private Account getOrCreateAccount() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        Timber.d("number of accounts: " + accounts.length);
        if (accounts.length != 0) {
            return accounts[0];
        } else {
            Account account = new Account(AuthConstants.ACCOUNT_NAME, AuthConstants.ACCOUNT_TYPE);
            accountManager.addAccountExplicitly(account, null, null);
            return account;
        }
    }

    private Account getAccount() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }


    @Override
    public String getCurrentUserUid() {
        UserProfile userProfile = realmService.loadUserProfile();
        return userProfile == null ? null : userProfile.getUid();
    }

    @Override
    public String getCurrentUserMsisdn() {
        UserProfile userProfile = realmService.loadUserProfile();
        return userProfile == null ? null : userProfile.getMsisdn();
    }
}
