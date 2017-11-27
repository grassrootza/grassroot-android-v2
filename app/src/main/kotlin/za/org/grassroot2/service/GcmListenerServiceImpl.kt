package za.org.grassroot2.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import timber.log.Timber
import za.org.grassroot2.GrassrootNotification
import za.org.grassroot2.R
import za.org.grassroot2.view.activity.DashboardActivity
import za.org.grassroot2.view.activity.MeetingDetailsActivity


/**
 * Created by bigor on 25.11.17..
 */

class GcmListenerServiceImpl : GcmListenerService() {


    override fun onMessageReceived(from: String, data: Bundle) {

        val message = GrassrootNotification.fromBundle(data)

        Timber.d("From: " + from)
        Timber.d("Message: " + message.text)

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        showNotification(message)
    }


    private fun showNotification(notification: GrassrootNotification) {

        val intent = if (notification.entityType == "MEETING") {
            val int = Intent(this, MeetingDetailsActivity::class.java)
            int.putExtra(MeetingDetailsActivity.EXTRA_MEETING_UID, notification.entytyId)
            int.putExtra(MeetingDetailsActivity.TRIGGERED_BY_NOTIFICATION, true)
        } else
            Intent(this, DashboardActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(notification.title)
                .setContentText(notification.text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}