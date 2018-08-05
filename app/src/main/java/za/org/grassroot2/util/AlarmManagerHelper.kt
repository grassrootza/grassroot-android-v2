package za.org.grassroot2.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import java.util.concurrent.TimeUnit

object AlarmManagerHelper {

    fun scheduleAlarmForBroadcastReceiver(c: Context, cls: Class<*>) {
        val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(c, cls)
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), PendingIntent.getBroadcast(c, 0, i, 0))
    }

    fun cancelAlarmForBroadcastReceiver(c: Context, cls: Class<*>) {
        val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(c, cls)
        alarmManager.cancel(PendingIntent.getBroadcast(c, 0, i, 0))
    }
}
