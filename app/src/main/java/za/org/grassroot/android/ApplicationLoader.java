package za.org.grassroot.android;

import android.app.Application;
import android.content.Context;

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
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // todo: build a release tree
    }
}
