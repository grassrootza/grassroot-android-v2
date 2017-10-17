package za.org.grassroot2.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import timber.log.Timber;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.UserProfile;
import za.org.grassroot2.services.account.AuthConstants;

public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountManager accountManager;
    private final DatabaseService databaseService;

    private static final String CONTENT_AUTHORITY = "za.org.grassroot2.syncprovider";

    @Inject
    public UserDetailsServiceImpl(AccountManager accountManager, DatabaseService databaseService) {
        this.accountManager = accountManager;
        this.databaseService = databaseService;
    }

    public Single<UserProfile> storeUserDetails(final String userUid,
                                                final String userPhone,
                                                final String userDisplayName,
                                                final String userSystemRole,
                                                final String userToken) {
        return Single.create(e -> {
            Account account = setAuthToken(userToken);
            Timber.v("stored auth token, number accounts = " + accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE).length);
            UserProfile userProfile = databaseService.updateOrCreateUserProfile(userUid, userPhone, userDisplayName, userSystemRole);
            setAccountAsSyncable(account);
            e.onSuccess(userProfile);
        });
    }

    @Override
    public Account setAuthToken(String userToken) {
        final Account account = getOrCreateAccount();
        accountManager.setAuthToken(account, AuthConstants.AUTH_TOKENTYPE, userToken);
        accountManager.setUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN, userToken);
        accountManager.setUserData(account, AuthConstants.USER_DATA_LOGGED_IN, "true");
        return account;
    }

    // todo: disposableOnDetach exception handling, also calls to server, GCM, etc
    @Override
    public Single<Boolean> logout(final boolean deleteAndroidAccount, final boolean wipeDb) {
        return Single.create(e -> {
            // first, wipe the details stored in account
            Account account = getAccount();
            if (account != null) {
                ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 0);
                accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE,
                        accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE));
                accountManager.setPassword(account, null);
                accountManager.setUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN, null);
                accountManager.setUserData(account, AuthConstants.USER_DATA_LOGGED_IN, null);
                if (deleteAndroidAccount) {
                    // using deprecated because non-deprecated requires API22+ .. oh Android
                    accountManager.removeAccount(account, null, null);
                }
            }
            databaseService.removeUserProfile(); // then, wipe the UID etc
            if (wipeDb) {
                databaseService.wipeDatabase();
            }
            e.onSuccess(true);
        });
    }

    @Override
    public boolean isSyncCompleted() {
        UserProfile userProfile = databaseService.loadUserProfile();
        return userProfile != null && userProfile.isSyncComplete();
    }

    @Override
    public boolean isSyncFailed() {
        UserProfile userProfile = databaseService.loadUserProfile();
        return userProfile != null && userProfile.isSyncFailed();
    }

    @Override
    public void setSyncState(int status) {
        UserProfile userProfile = databaseService.loadUserProfile();
        if (status==UserProfile.SYNC_STATE_FAILED && userProfile.getSyncStatus() != UserProfile.SYNC_STATE_NONE) {
            return;
        } else {
            userProfile.setSyncState(status);
            databaseService.storeObject(UserProfile.class, userProfile);
        }
    }

    public String getCurrentToken() {
        Account account = getAccount();
        return account == null ? null : accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private Account getOrCreateAccount() {
        Timber.d("getting or creating account for Grassroot ...");
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        Timber.d("number of accounts: " + accounts.length);
        Account account;
        if (accounts.length != 0) {
            account = accounts[0];
        } else {
            account = new Account(AuthConstants.ACCOUNT_NAME, AuthConstants.ACCOUNT_TYPE);
            Timber.d("adding account explicitly");
            accountManager.addAccountExplicitly(account, null, null);
            Timber.d("setting account as syncable");
        }
        return account;
    }

    // warehousing
    private void setAccountAsSyncable(Account account) {
        final String AUTHORITY = CONTENT_AUTHORITY;
        final long SYNC_FREQUENCY = TimeUnit.MINUTES.toMillis(15);

        // Inform the system that this account supports sync
        ContentResolver.setIsSyncable(account, AUTHORITY, 1);

        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

        // Recommend a schedule for automatic synchronization. The system may modify this based
        // on other scheduled syncs and network utilization.
        ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
    }

    private Account getAccount() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }


    @Override
    public String getCurrentUserUid() {
        UserProfile userProfile = databaseService.loadUserProfile();
        // todo: throw an error if this is null, which should trigger a user logout
        final String userUid = userProfile == null ? null : userProfile.getUid();
        return userUid;
    }

    @Override
    public String getCurrentUserMsisdn() {
        UserProfile userProfile = databaseService.loadUserProfile();
        return userProfile == null ? null : userProfile.getMsisdn();
    }

    @Override
    public void requestSync() {
        Timber.d("requesting a sync ...");
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getAccount(), CONTENT_AUTHORITY, b);

    }

    @Override
    public void cleanUpForActivity() {
    }
}
