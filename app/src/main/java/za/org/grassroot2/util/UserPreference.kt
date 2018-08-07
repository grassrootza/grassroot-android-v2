package za.org.grassroot2.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import javax.inject.Inject

class UserPreference @Inject
constructor(private val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setNoConnectionInfoDisplayed(displayed: Boolean) {
        prefs.edit().putBoolean(PREF_OFFLINE_DIALOG_DISPLAYED, displayed).apply()
    }

    fun connectionInfoDisplayed(): Boolean {
        return prefs.getBoolean(PREF_OFFLINE_DIALOG_DISPLAYED, false)
    }

    companion object {
        private const val PREF_OFFLINE_DIALOG_DISPLAYED = "offline_dialog_displayed"
    }

}
