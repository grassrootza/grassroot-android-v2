package za.org.grassroot2.util

import android.content.Context

import java.util.Calendar
import java.util.concurrent.TimeUnit

import za.org.grassroot2.R

object LastModifiedFormatter {

    fun lastSeen(c: Context, datetimeInMillis: Long): String {
        var out = ""
        try {
            val calendar = Calendar.getInstance()
            val diff = calendar.timeInMillis - datetimeInMillis
            val difSeconds = TimeUnit.MILLISECONDS.toSeconds(diff).toInt()
            val difMinutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
            val difHours = TimeUnit.MILLISECONDS.toHours(diff).toInt()
            val difDay = TimeUnit.MILLISECONDS.toDays(diff).toInt()
            val difMonth = difDay / 30
            val difYear = difMonth / 12
            if (difSeconds <= 0) {
                out = "Now"
            } else if (difSeconds < 60) {
                out = c.resources.getQuantityString(R.plurals.last_modified, difSeconds, difSeconds, "second")
            } else if (difMinutes < 60) {
                out = c.resources.getQuantityString(R.plurals.last_modified, difMinutes, difMinutes, "minute")
            } else if (difHours < 24) {
                out = c.resources.getQuantityString(R.plurals.last_modified, difHours, difHours, "hour")
            } else if (difDay < 30) {
                out = c.resources.getQuantityString(R.plurals.last_modified, difDay, difDay, "day")
            } else if (difMonth < 12) {
                out = c.resources.getQuantityString(R.plurals.last_modified, difMonth, difMonth, "month")
            } else {
                out = c.resources.getQuantityString(R.plurals.last_modified, difYear, difYear, "year")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return out
    }
}
