package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import kotlinx.android.synthetic.main.item_member_log.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.Membership
import za.org.grassroot2.model.MembershipLog
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by luke on 2017/12/10.
 */
class MemberLogsAdapter @Inject
constructor(private val context: Context, private var data: List<MembershipLog>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredData : List<MembershipLog> = data.toList()
    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

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
        (holder as MembershipLogViewHolder).iconHolder.setImageResource(getIconForLogType(memberLog.changeType))
        setHeaderForType(memberLog, holder)
        holder.logDetails.text = getDescriptionForLogType(memberLog)
    }

    private fun getIconForLogType(changeType: String) = when (changeType) {
        "GROUP_MEMBER_ADDED", "GROUP_MEMBER_ADDED_AT_CREATION", "GROUP_MEMBER_ADDED_VIA_CAMPAIGN",
        "GROUP_MEMBER_ADDED_VIA_JOIN_CODE" -> R.drawable.ic_plus_1_green_24dp
        "GROUP_MEMBER_REMOVED" -> R.drawable.ic_minus_1_red_24dp
        "GROUP_MEMBER_ROLE_CHANGED" -> R.drawable.ic_member_history_default
        else -> R.drawable.ic_member_history_default
    }

    private fun setHeaderForType(memberLog: MembershipLog, holder: MembershipLogViewHolder) {
        when (memberLog.changeType) {
            "GROUP_MEMBER_ADDED", "GROUP_MEMBER_ADDED_AT_CREATION", "GROUP_MEMBER_ADDED_VIA_CAMPAIGN", "GROUP_MEMBER_ADDED_VIA_JOIN_CODE" -> {
                holder.changeType.text = context.getString(R.string.member_added_header)
            }
            "GROUP_MEMBER_REMOVED" -> {
                holder.changeType.text = context.getString(R.string.member_removed_header)
                holder.changeType.setTextColor(ContextCompat.getColor(context, R.color.light_red))
            }
            "GROUP_MEMBER_ROLE_CHANGED" -> {
                holder.changeType.text = context.getString(R.string.member_role_changed_header)
                holder.changeType.setTextColor(ContextCompat.getColor(context, R.color.text_dark_grey))
            }
        }
    }

    // todo : distinguish in removed types btw removed self and removed other
    private fun getDescriptionForLogType(log: MembershipLog) : String {
        val dateFormatted = dateFormat.format(Date(log.changeDateTimeMillis))
        return when (log.changeType) {
            "GROUP_MEMBER_ADDED" -> context.getString(R.string.member_added_def_desc, log.memberName, log.changingUserName, dateFormatted)
            "GROUP_MEMBER_ADDED_AT_CREATION" -> context.getString(R.string.member_added_creation, log.memberName, dateFormatted)
            "GROUP_MEMBER_ADDED_VIA_CAMPAIGN" -> context.getString(R.string.member_added_via_join_code, log.memberName, dateFormatted)
            "GROUP_MEMBER_ADDED_VIA_JOIN_CODE" -> context.getString(R.string.member_added_via_campaign, log.memberName, dateFormatted)
            "GROUP_MEMBER_REMOVED" -> context.getString(R.string.member_removed_other, log.memberName, log.changingUserName, dateFormatted)
            "GROUP_MEMBER_ROLE_CHANGED" -> context.getString(R.string.member_role_changed_desc,
                    log.changingUserName, log.memberName, dateFormatted, context.getString(Membership.getRoleNameResource(log.description)))
            else -> context.getString(R.string.member_changed_other, log.memberName, dateFormatted)
        }
    }

    internal class MembershipLogViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.root
        var iconHolder = itemView.logIcon
        var changeType = itemView.memberChangeType
        var logDetails  = itemView.logDetails
    }

}