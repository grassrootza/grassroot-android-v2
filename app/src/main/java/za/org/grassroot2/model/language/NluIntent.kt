package za.org.grassroot2.model.language

import za.org.grassroot2.R

/**
 * Created by luke on 2017/11/14.
 */
data class NluIntent(val name: String, val confidence: Float) {

    companion object {
        const val MEETING_INTENT = "set_meeting"
        const val LIVEWIRE_INTENT = "create_livewire"
        const val GROUP_CREATE_INTENT = "group_create_intent"
    }

    fun getActionEquivalent(): Int {
        when (name) {
            MEETING_INTENT -> return R.id.call_meeting
            LIVEWIRE_INTENT -> return R.id.create_livewire_alert
            GROUP_CREATE_INTENT -> return R.id.create_group
            else -> {
                return R.id.unknownIntent
            }
        }
    }

}