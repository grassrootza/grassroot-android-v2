package za.org.grassroot2.util;

import android.content.Context;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import za.org.grassroot2.R;

public class LastModifiedFormatter {

    public static String lastSeen(Context c, long datetimeInMillis) {
        String out = "";
        try {
            Calendar calendar = Calendar.getInstance();
            long diff = calendar.getTimeInMillis() - datetimeInMillis;
            int difSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(diff);
            int difMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);
            int difHours = (int) TimeUnit.MILLISECONDS.toHours(diff);
            int difDay = (int) TimeUnit.MILLISECONDS.toDays(diff);
            int difMonth = difDay / 30;
            int difYear = difMonth / 12;
            if (difSeconds <= 0) {
                out = "Now";
            } else if (difSeconds < 60) {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difSeconds, difSeconds, "second");
            } else if (difMinutes < 60) {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difMinutes, difMinutes, "minute");
            } else if (difHours < 24) {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difHours, difHours, "hour");
            } else if (difDay < 30) {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difDay, difDay, "day");
            } else if (difMonth < 12) {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difMonth, difMonth, "month");
            } else {
                out = c.getResources().getQuantityString(R.plurals.last_modified, difYear, difYear, "year");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }
}
