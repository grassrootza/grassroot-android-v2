package za.org.grassroot2

import android.os.Bundle
import java.util.*

/**
 * Created by bigor on 26.11.17..
 */
class GrassrootNotification(

        val creationTime: Date,
        val gcmSendTime: Date,
        val gcmMsgId: String,
        val notificationUid: String,

        val title: String,
        val text: String,
        val entityType: String,
        val entytyId: String,
        val action: String,

        val groupId: String?,
        val groupName: String?,

        val collapseKey: String,
        val alertType: String
) {
    companion object {

        fun fromBundle(data: Bundle): GrassrootNotification {

            return GrassrootNotification(
                    Date(data.getLong("created_date_time")),
                    Date(data.getLong("google.sent_time")),
                    data.getString("google.message_id"),
                    data.getString("notification_uid"),
                    data.getString("title"),
                    data.getString("body"),
                    data.getString("entity_type"),
                    data.getString("entity_uid"),
                    data.getString("click_action"),
                    data.getString("group_uid"),
                    data.getString("group"),
                    data.getString("collapse_key"),
                    data.getString("alert_type")
            )
        }
    }
}
