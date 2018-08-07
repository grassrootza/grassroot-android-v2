package za.org.grassroot2.util


import android.text.format.DateFormat

object DateFormatter {

    private const val TIME_FORMAT = "hh:mm a"
    private const val DATE_FORMAT = "EEE, MMMM d"
    private const val YEAR_FORMAT = "yyyy"
    private const val MEETING_DATE_FORMAT = "E d of MMMM 'at' hh:mm"

    fun formatTime(timestamp: Long): String {
        return DateFormat.format(TIME_FORMAT, timestamp).toString()
    }

    fun formatMeetingDate(timestamp: Long): String {
        return DateFormat.format(MEETING_DATE_FORMAT, timestamp).toString()
    }

    fun formatDate(timestamp: Long): String {
        return DateFormat.format(DATE_FORMAT, timestamp).toString()
    }

    fun formatYear(timestamp: Long): String {
        return DateFormat.format(YEAR_FORMAT, timestamp).toString()
    }
}
