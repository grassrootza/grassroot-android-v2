package za.org.grassroot.android.services.auth;

import android.accounts.Account;
import android.accounts.AccountManager;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.realm.Realm;
import za.org.grassroot.android.model.UserProfile;

public class UserDetailsServiceImpl implements UserDetailsService {

    private AccountManager accountManager;

    @Inject
    public UserDetailsServiceImpl(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public Single<UserProfile> storeUserDetails(final String userUid,
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
                realm.close();
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
                // then, wipe the UID etc
                Realm realm = Realm.getDefaultInstance();
                final UserProfile userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
                if (userProfile != null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            userProfile.deleteFromRealm();
                        }
                    });
                }
                e.onSuccess(true);
                realm.close();
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
                        Realm realm = Realm.getDefaultInstance();
                        realm.deleteAll();
                        realm.close();
                        return true;
                    }
                });
    }

    public String getCurrentToken() {
        Account account = getAccount();
        return account == null ? null : accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private Account getAccount() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }


    @Override
    public String getCurrentUserUid() {
        Realm realm = Realm.getDefaultInstance();
        UserProfile userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
        final String uid = userProfile == null ? null : userProfile.getUid();
        realm.close();
        return uid;
    }

    @Override
    public String getCurrentUserMsisdn() {
        Realm realm = Realm.getDefaultInstance();
        UserProfile userProfile = realm.where(UserProfile.class).equalTo("id", 0).findFirst();
        final String msisdn = userProfile == null ? null : userProfile.getMsisdn();
        realm.close();
        return msisdn;
    }
}
