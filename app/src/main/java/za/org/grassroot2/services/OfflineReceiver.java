package za.org.grassroot2.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.util.UserPreference;

public class OfflineReceiver extends BroadcastReceiver {

    @Inject UserPreference userPrefs;

    public static final String ACTION_RESET_OFFLINE_INFO = "za.org.grassroot2" + ".RESET_OFFLINE_INFO";

    @Override
    public void onReceive(Context context, Intent intent) {
        ((GrassrootApplication)context.getApplicationContext()).getAppComponent().inject(this);
        userPrefs.setNoConnectionInfoDisplayed(false);
    }
}
