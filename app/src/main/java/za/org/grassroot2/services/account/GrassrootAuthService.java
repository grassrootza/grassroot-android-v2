package za.org.grassroot2.services.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.services.rest.GrassrootAuthApi;

public class GrassrootAuthService extends Service {

    private GrassrootAuthApi grassrootAuthApi;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.e("creating auth service");

        ((GrassrootApplication) (getApplicationContext()))
                .getAppComponent().inject(this);
    }

    @Inject
    public void setGrassrootAuthApi(GrassrootAuthApi grassrootAuthApi) {
        this.grassrootAuthApi = grassrootAuthApi;
    }

    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        Timber.e("bound the auth service");
        return authenticator.getIBinder();
    }

}
