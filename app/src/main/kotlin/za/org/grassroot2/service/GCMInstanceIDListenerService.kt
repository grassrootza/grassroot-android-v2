package za.org.grassroot2.service

import android.content.Intent
import com.google.android.gms.iid.InstanceIDListenerService


/**
 * Created by bigor on 25.11.17..
 */
class GCMInstanceIDListenerService : InstanceIDListenerService() {


    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        val intent = Intent(this, RegistrationService::class.java)
        startService(intent)
    }
}