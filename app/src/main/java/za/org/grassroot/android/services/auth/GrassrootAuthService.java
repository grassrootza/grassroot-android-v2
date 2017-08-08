package za.org.grassroot.android.services.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import timber.log.Timber;

public class GrassrootAuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("bound the auth service");
        AccountAuthenticator authenticator = new AccountAuthenticator(getApplication());
        return authenticator.getIBinder();
    }
}
