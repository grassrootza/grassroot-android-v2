package za.org.grassroot2.model.language

/**
 * Created by luke on 2017/11/14.
 */
data class NluResponse(val conversationUid: String, val intent: NluIntent, val entities: List<Entity>) {
}