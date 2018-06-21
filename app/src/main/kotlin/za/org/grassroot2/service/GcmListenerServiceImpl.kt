package za.org.grassroot2.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.NotificationCompat
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.gcm.GcmListenerService
import com.google.android.gms.gcm.GoogleCloudMessaging
import timber.log.Timber
import za.org.grassroot.messaging.dto.EventNotificationDTO
import za.org.grassroot.messaging.dto.EventType
import za.org.grassroot.messaging.dto.MessageDTO
import za.org.grassroot2.R
import za.org.grassroot2.view.activity.DashboardActivity
import za.org.grassroot2.view.activity.MeetingDetailsActivity
import za.org.grassroot2.view.activity.VoteDetailsActivity
import java.io.IOException
import java.util.*


/**
 * Created by bigor on 25.11.17..
 */

class GcmListenerServiceImpl : GcmListenerService() {


    private val objectMapper = ObjectMapper()

    override fun onMessageReceived(from: String, data: Bundle) {

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            val messageId = data.getString("google.message_id").substringAfterLast(':')
            val msgJson = data.getString("body")
            val message = objectMapper.readValue(msgJson, MessageDTO::class.java)
            showNotification(message)
            sendDeliveryReceipt(messageId)
        }
    }

    private fun sendDeliveryReceipt(messageId: String) {
        val gcm: GoogleCloudMessaging = GoogleCloudMessaging.getInstance(this)
        try {
            val data = Bundle()
            data.putString("action", "DELIVERY_RECEIPT")
            data.putString("delivered_message_id", messageId)

            val id = UUID.randomUUID().toString()
            val senderId = getString(R.string.gcm_sender_id)
            gcm.send(senderId + "@gcm.googleapis.com", id, data)
        } catch (ex: IOException) {
            Timber.e(ex)
        }
    }


    private fun showNotification(msg: MessageDTO) {

        val intent = getTargetIntentForMessage(msg)

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


    private fun getTargetIntentForMessage(msg: MessageDTO): Intent {

        return when (msg) {
            is EventNotificationDTO -> {
                if (msg.eventType == EventType.MEETING) {
                    val meetingIntent = Intent(this, MeetingDetailsActivity::class.java)
                    meetingIntent.putExtra(MeetingDetailsActivity.EXTRA_MEETING_UID, msg.eventUid)
                    meetingIntent.putExtra(MeetingDetailsActivity.TRIGGERED_BY_NOTIFICATION, true)
                } else if (msg.eventType == EventType.VOTE) {
                    val voteIntent = Intent(this, VoteDetailsActivity::class.java)
                    voteIntent.putExtra(VoteDetailsActivity.EXTRA_VOTE_UID, msg.eventUid)
                    voteIntent.putExtra(VoteDetailsActivity.TRIGGERED_BY_NOTIFICATION, true)
                } else {
                    defaultTargetIntent(msg)
                }
            }
            else -> {
                defaultTargetIntent(msg)
            }
        }
    }

    private fun defaultTargetIntent(msg: MessageDTO): Intent {
        val dashboardIntent = Intent(this, DashboardActivity::class.java)
        return dashboardIntent.putExtra("notificationText", msg.text)
    }
}