package za.org.grassroot.android;

import android.app.Application;
import android.os.Debug;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import timber.log.Timber;
import za.org.grassroot.android.dagger.AppComponent;
import za.org.grassroot.android.dagger.AppModule;
import za.org.grassroot.android.dagger.DaggerAppComponent;
import za.org.grassroot.android.dagger.user.UserComponent;

public class GrassrootApplication extends Application {

    private AppComponent appComponent;
    private UserComponent userComponent;

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
        Debug.startMethodTracing("app_loader");
        appComponent = initDagger(this);
        initTimber();
        initRealmConfiguration();
        Debug.stopMethodTracing();
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {  // todo: build a release tree
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initRealmConfiguration() {
        Timber.i("Starting Realm configuration");
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        if (!BuildConfig.DEBUG || skipMigrationTest) {
            safeCheckRealmMigration(realmConfiguration);
        }
    }

    private void safeCheckRealmMigration(RealmConfiguration config) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.close();
        } catch (RealmMigrationNeededException e) {
            Timber.e("Error! Should have migrated");
            Realm.deleteRealm(config);
        }
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
