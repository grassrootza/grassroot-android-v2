package za.org.grassroot2.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


final public class NetworkUtil {

    public static boolean hasInternetAccess(Context context) {
        boolean hasInternet = false;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            hasInternet = networkInfo.isConnectedOrConnecting();
        }
        return hasInternet;
    }
}
