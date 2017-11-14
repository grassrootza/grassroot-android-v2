package za.org.grassroot2.model.language

import za.org.grassroot2.R

/**
 * Created by luke on 2017/11/14.
 */
class NluIntent(val intentName: String, val confidence: Float) {

    companion object {
        const val MEETING_INTENT = "set_meeting"
        const val LIVEWIRE_INTENT = "create_livewire"
        const val GROUP_CREATE_INTENT = "group_create_intent"
    }

    fun getActionEquivalent(): Int {
        when (intentName) {
            MEETING_INTENT -> return R.id.callMeeting
            LIVEWIRE_INTENT -> return R.id.createLivewireAlert
            GROUP_CREATE_INTENT -> return R.id.createGroup
            else -> {
                return R.id.unknownIntent
            }
        }
    }

}