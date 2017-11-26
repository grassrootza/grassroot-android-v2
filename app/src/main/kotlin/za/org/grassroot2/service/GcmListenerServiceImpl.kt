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


    private fun showNotification(message: GrassrootNotification) {

        val intent = if (message.entityType == "MEETING") {
            val int = Intent(this, MeetingDetailsActivity::class.java)
            int.putExtra(MeetingDetailsActivity.EXTRA_MEETING_UID, message.entytyId)
        } else
            Intent(this, DashboardActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle("GCM Message")
                .setContentText(message.text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}