package za.org.grassroot2.services.account

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

import timber.log.Timber

class SyncService : Service() {

    override fun onCreate() {
        super.onCreate()
        Timber.e("creating sync service")
        // SyncAdapter is not Thread-safe
        synchronized(LOCK) {
            syncAdapter = SyncAdapter(this, false, false)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.e("binding sync adapter")
        // Return our SyncAdapter's IBinder
        return syncAdapter!!.syncAdapterBinder
    }

    companion object {

        private val LOCK = Any()
        private var syncAdapter: SyncAdapter? = null
    }
}
