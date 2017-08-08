package za.org.grassroot.android.services.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

public class GrassrootAuthServiceImpl extends Service implements GrassrootAuthService{

    private GrassrootAuthApi grassrootAuthApi;
    private AccountManager accountManager;

    @Inject
    public GrassrootAuthServiceImpl(GrassrootAuthApi grassrootAuthApi, AccountManager accountManager) {
        this.grassrootAuthApi = grassrootAuthApi;
        this.accountManager = accountManager;
        Timber.i("rest service loaded? : " + (grassrootAuthApi != null));
        Timber.i("accountManager loaded? : " + (accountManager != null));
    }

    public String getToken() {
        Account account = getAccount();
        return account == null ? null : accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private Account getAccount() {
        Account[] accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }

    @Override
    public void logoutCleanSweap() {


    }

    @Override
    public void logoutRetainData() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("bound the auth service");
        AccountAuthenticator authenticator = new AccountAuthenticator(getApplication(), grassrootAuthApi);
        return authenticator.getIBinder();
    }

}
