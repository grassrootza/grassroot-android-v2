package za.org.grassroot.android.services.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot.android.services.rest.GrassrootAuthApi;

public class GrassrootAuthServiceImpl extends Service {

    private GrassrootAuthApi grassrootAuthApi;

    @Inject
    public GrassrootAuthServiceImpl(GrassrootAuthApi grassrootAuthApi) {
        this.grassrootAuthApi = grassrootAuthApi;
        Timber.i("rest service loaded? : " + (grassrootAuthApi != null));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("bound the auth service");
        AccountAuthenticator authenticator = new AccountAuthenticator(getApplication(), grassrootAuthApi);
        return authenticator.getIBinder();
    }

}
