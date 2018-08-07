package za.org.grassroot2.services.account

import android.app.Service
import android.content.Intent
import android.os.IBinder

class GrassrootAuthService : Service() {

//    override fun onBind(intent: Intent): IBinder? {
//        val authenticator = AccountAuthenticator(this)
//        Timber.e("bound the auth service")
//        return authenticator.iBinder
//    }

    override fun onBind(intent: Intent): IBinder? = AccountAuthenticator(this).iBinder

}
