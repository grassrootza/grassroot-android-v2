package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import kotlinx.android.synthetic.main.item_member_log.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.MembershipLog
import javax.inject.Inject

/**
 * Created by luke on 2017/12/10.
 */
class MemberLogsAdapter @Inject
constructor(private val context: Context, private var data: List<MembershipLog>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredData : List<MembershipLog> = data.toList()

    override fun getItemCount(): Int = filteredData.size

    override fun getFilter(): Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            val filtered = data.filter { membership -> membership.containsString(constraint) }
            results.values = filtered
            results.count = filtered.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filteredData = results.values as List<MembershipLog>
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            MembershipLogViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_member_log, parent, false))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val memberLog = filteredData[position]
        (holder as MembershipLogViewHolder).changeType.text = getHeaderForLogType(memberLog.changeType)
        holder.logDetails.text = getDescriptionForLogType(memberLog)
    }

    private fun getHeaderForLogType(changeType: String) = when (changeType) {
            "GROUP_MEMBER_ADDED" -> context.getString(R.string.member_added_header)
            "GROUP_MEMBER_ADDED_AT_CREATION" -> context.getString(R.string.member_added_header)
            "GROUP_MEMBER_ADDED_VIA_CAMPAIGN" -> context.getString(R.string.member_added_header)
            "GROUP_MEMBER_ADDED_VIA_JOIN_CODE" -> context.getString(R.string.member_added_header)
            "GROUP_MEMBER_REMOVED" -> context.getString(R.string.member_removed_header)
            "GROUP_MEMBER_ROLE_CHANGED" -> context.getString(R.string.member_role_changed_header)
            else -> context.getString(R.string.member_change_default_header)
    }

    // todo : distinguish in removed types btw removed self and removed other
    private fun getDescriptionForLogType(log: MembershipLog) = when (log.changeType) {
        "GROUP_MEMBER_ADDED" -> context.getString(R.string.member_added_def_desc, log.memberName, log.changingUserName, "Date")
        "GROUP_MEMBER_ADDED_AT_CREATION" -> context.getString(R.string.member_added_creation, log.memberName, "Date")
        "GROUP_MEMBER_ADDED_VIA_CAMPAIGN" -> context.getString(R.string.member_added_via_join_code, log.memberName, "Date")
        "GROUP_MEMBER_ADDED_VIA_JOIN_CODE" -> context.getString(R.string.member_added_via_campaign, log.memberName, "Date")
        "GROUP_MEMBER_REMOVED" -> context.getString(R.string.member_removed_other, log.memberName, log.changingUserName, "Date")
        "GROUP_MEMBER_ROLE_CHANGED" -> context.getString(R.string.member_role_changed_desc, log.memberName, "Date", log.description)
        else -> context.getString(R.string.member_changed_other, log.memberName, "Date")
    }


    internal class MembershipLogViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.root
        var changeType = itemView.memberChangeType
        var logDetails  = itemView.logDetails
    }

}