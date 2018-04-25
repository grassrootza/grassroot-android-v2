package za.org.grassroot2.service

import android.app.IntentService
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import dagger.Lazy
import timber.log.Timber
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.R
import za.org.grassroot2.services.rest.GrassrootUserApi
import javax.inject.Inject


/**
 * Created by bigor on 25.11.17..
 */
class GCMRegistrationService : IntentService(TAG) {


    @Inject
    lateinit var grassrootUserApi: Lazy<GrassrootUserApi>

    companion object {
        val TAG = "RegIntentService"
    }

    override fun onCreate() {
        super.onCreate()
        (application as GrassrootApplication).appComponent.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

//        try {
//
//            val instanceID = InstanceID.getInstance(this)
//
//            val gcmSenderId = getString(R.string.gcm_sender_id)
//            val token = instanceID.getToken(gcmSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
//
//            Timber.i("GCM Registration Token: " + token)
//
//            val currentToken = sharedPreferences.getString(GCMPreferences.CURRENT_GCM_TOKEN, null)
//
//            if (currentToken == null || currentToken != token) {
////                sendRegistrationToServer(token, sharedPreferences)
//            } else {
//                val registrationComplete = Intent(GCMPreferences.GCM_REGISTRATION_COMPLETE)
//                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
//            }
//
//        } catch (e: Exception) {
//            val registrationFailed = Intent(GCMPreferences.GCM_REGISTRATION_COMPLETE)
//            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationFailed)
//            Timber.d("Failed to complete token refresh", e)
//        }

    }

    private fun sendRegistrationToServer(token: String, preferances: SharedPreferences) {

        grassrootUserApi.get().registerGCMToken(token)
                .subscribe(
                        { success ->
                            if (success) {
                                preferances.edit().putString(GCMPreferences.CURRENT_GCM_TOKEN, token).apply()
                                val registrationComplete = Intent(GCMPreferences.GCM_REGISTRATION_COMPLETE)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
                            }
                        },
                        { error ->
                            Timber.e(error, "Failed to send gcm token to server")
                            val registrationFailed = Intent(GCMPreferences.GCM_REGISTRATION_COMPLETE)
                            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationFailed)
                        }
                )
    }


}