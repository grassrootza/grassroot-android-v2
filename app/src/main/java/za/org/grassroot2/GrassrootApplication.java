package za.org.grassroot2;

import android.app.Application;
import android.os.Debug;

import timber.log.Timber;
import za.org.grassroot2.dagger.AppComponent;
import za.org.grassroot2.dagger.AppModule;
import za.org.grassroot2.dagger.DaggerAppComponent;

public class GrassrootApplication extends Application {

    private AppComponent appComponent;

    // since we sometimes want to enforce Realm not deleting itself during development
    private static final boolean skipMigrationTest = true;

    protected AppComponent initDagger(GrassrootApplication application) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
//        Debug.startMethodTracing("app_loader");
        appComponent = initDagger(this);
        initTimber();
        Debug.stopMethodTracing();
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
