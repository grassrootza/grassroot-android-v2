package za.org.grassroot.android;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import timber.log.Timber;
import za.org.grassroot.android.dagger.AppComponent;
import za.org.grassroot.android.dagger.AppModule;
import za.org.grassroot.android.dagger.DaggerAppComponent;

public class GrassrootApplication extends Application {

    private AppComponent appComponent;
    public static volatile Context applicationContext;

    protected AppComponent initDagger(GrassrootApplication application) {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = initDagger(this);
        applicationContext = getApplicationContext();
        // todo: build a release tree
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Realm.init(applicationContext);
        RealmConfiguration.Builder realmConfigBuilder =
                new RealmConfiguration.Builder();
        Realm.setDefaultConfiguration(realmConfigBuilder.build());

        try {
            Realm realm = Realm.getDefaultInstance();
            realm.close();
        } catch (RealmMigrationNeededException e) {
            Timber.e("Error! Should have migrated");
            Realm.deleteRealm(realmConfigBuilder.build());
        }
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
