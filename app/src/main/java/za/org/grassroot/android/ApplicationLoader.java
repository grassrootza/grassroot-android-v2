package za.org.grassroot.android;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import timber.log.Timber;

/**
 * Created by luke on 2017/07/26.
 */

public class ApplicationLoader extends Application {

    public static volatile Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
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
}
