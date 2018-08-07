package za.org.grassroot2.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


object NetworkUtil {

    fun hasInternetAccess(context: Context): Boolean {
        var hasInternet = false
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null) {
            hasInternet = networkInfo.isConnectedOrConnecting
        }
        return hasInternet
    }
}
