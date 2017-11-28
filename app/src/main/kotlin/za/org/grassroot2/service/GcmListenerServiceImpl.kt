package za.org.grassroot2.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.NotificationCompat
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.gcm.GcmListenerService
import za.org.grassroot.messaging.dto.EventNotificationDTO
import za.org.grassroot.messaging.dto.EventType
import za.org.grassroot.messaging.dto.MessageDTO
import za.org.grassroot2.R
import za.org.grassroot2.view.activity.DashboardActivity
import za.org.grassroot2.view.activity.MeetingDetailsActivity


/**
 * Created by bigor on 25.11.17..
 */

class GcmListenerServiceImpl : GcmListenerService() {


    private val objectMapper = ObjectMapper()

    override fun onMessageReceived(from: String, data: Bundle) {

        val msgJson = data.getString("body")
        val message = objectMapper.readValue(msgJson, MessageDTO::class.java)


        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        showNotification(message)
    }


    private fun showNotification(msg: MessageDTO) {

        val intent =
                if (msg is EventNotificationDTO && msg.eventType == EventType.MEETING) {
                    val meetingIntent = Intent(this, MeetingDetailsActivity::class.java)
                    meetingIntent.putExtra(MeetingDetailsActivity.EXTRA_MEETING_UID, msg.eventUid)
                    meetingIntent.putExtra(MeetingDetailsActivity.TRIGGERED_BY_NOTIFICATION, true)
                } else {
                    val dashboardIntent = Intent(this, DashboardActivity::class.java)
                    dashboardIntent.putExtra("notificationText", msg.text)
                }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(msg.title)
                .setContentText(msg.text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}