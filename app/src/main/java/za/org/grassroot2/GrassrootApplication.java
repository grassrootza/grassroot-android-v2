package za.org.grassroot2;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.AppModule;
import za.org.grassroot2.dagger.DaggerAppComponent;
import za.org.grassroot2.services.SyncOfflineDataService;
import za.org.grassroot2.util.NetworkUtil;

public class GrassrootApplication extends Application {

    private AppComponent appComponent;

    private BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtil.hasInternetAccess(context)) {
                startService(new Intent(context, SyncOfflineDataService.class));
            }
        }
    };

    protected AppComponent initDagger(GrassrootApplication application) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = initDagger(this);
        initTimber();
        Fabric.with(this, new Crashlytics());
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        registerReceiver(connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(connectivityChangeReceiver);
        super.onTerminate();
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {  // todo: build a release tree
            Timber.plant(new Timber.DebugTree());
        }
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
