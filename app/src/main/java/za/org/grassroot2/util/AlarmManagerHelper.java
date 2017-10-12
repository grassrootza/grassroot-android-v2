package za.org.grassroot2.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

public class AlarmManagerHelper {

    public static void scheduleAlarmForBroadcastReceiver(Context c, Class cls) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, cls);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), PendingIntent.getBroadcast(c, 0, i, 0));
    }

    public static void cancelAlarmForBroadcastReceiver(Context c, Class cls) {
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, cls);
        alarmManager.cancel(PendingIntent.getBroadcast(c, 0, i, 0));
    }
}
