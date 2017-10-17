package za.org.grassroot2.util;


import android.text.format.DateFormat;

public class DateFormatter {
    public static final String TIME_FORMAT = "hh:mm a";
    public static final String DATE_FORMAT = "EEE, MMMM d";
    public static final String YEAR_FORMAT = "yyyy";

    public static String formatTime(long timestamp) {
        return DateFormat.format(TIME_FORMAT, timestamp).toString();
    }

    public static String formatDate(long timestamp) {
        return DateFormat.format(DATE_FORMAT, timestamp).toString();
    }

    public static String formatYear(long timestamp) {
        return DateFormat.format(YEAR_FORMAT, timestamp).toString();
    }
}
