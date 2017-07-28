package za.org.grassroot.android.services.auth;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import timber.log.Timber;
import za.org.grassroot.android.ApplicationLoader;

/**
 * Created by luke on 2017/07/26.
 */
public class GrassrootAuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("bound the auth service");
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
