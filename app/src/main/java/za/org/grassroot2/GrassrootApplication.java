package za.org.grassroot2;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.AppModule;
import za.org.grassroot2.dagger.DaggerAppComponent;

public class GrassrootApplication extends Application {

    private AppComponent appComponent;

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
        Stetho.initializeWithDefaults(this);
        Fabric.with(this, new Crashlytics());
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
