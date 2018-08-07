package za.org.grassroot2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.util.UserPreference
import javax.inject.Inject

class OfflineReceiver : BroadcastReceiver() {

    @Inject lateinit var userPrefs: UserPreference

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as GrassrootApplication).appComponent.inject(this)
        userPrefs.setNoConnectionInfoDisplayed(false)
    }

    companion object {
        const val ACTION_RESET_OFFLINE_INFO = "za.org.grassroot2" + ".RESET_OFFLINE_INFO"
    }
}
