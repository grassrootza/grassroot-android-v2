package za.org.grassroot.android.services.account;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import timber.log.Timber;
import za.org.grassroot.android.BuildConfig;

public class SyncService extends Service {

    private static final Object LOCK = new Object();
    private static SyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.e("creating sync service");
        // SyncAdapter is not Thread-safe
        synchronized (LOCK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                syncAdapter = new SyncAdapter(this, false, false);
            } else {
                syncAdapter = new SyncAdapter(this, false);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.e("binding sync adapter");
        // Return our SyncAdapter's IBinder
        return syncAdapter.getSyncAdapterBinder();
    }
}
