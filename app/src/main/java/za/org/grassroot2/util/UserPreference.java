package za.org.grassroot2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;

import javax.inject.Inject;

public final class UserPreference {

    private static final String PREF_OFFLINE_DIALOG_DISPLAYED = "offline_dialog_displayed";
    private final Context context;
    private final SharedPreferences prefs;

    @Inject
    public UserPreference(Context c) {
        context = c;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setNoConnectionInfoDisplayed(boolean displayed) {
        prefs.edit().putBoolean(PREF_OFFLINE_DIALOG_DISPLAYED, displayed).apply();
    }

    public boolean connectionInfoDisplayed() {
        return prefs.getBoolean(PREF_OFFLINE_DIALOG_DISPLAYED, false);
    }

}
