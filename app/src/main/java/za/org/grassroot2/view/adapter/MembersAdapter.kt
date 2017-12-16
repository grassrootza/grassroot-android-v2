package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_member.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.Membership
import za.org.grassroot2.model.util.PhoneNumberFormatter
import za.org.grassroot2.rxbinding.RxView
import javax.inject.Inject

/**
 * Created by luke on 2017/12/10.
 */
class MembersAdapter @Inject
constructor(private val context: Context, private var data: List<Membership>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredData : List<Membership> = data.toList()

    private val viewClickSubject =  PublishSubject.create<Membership>()
    val viewClickObservable: Observable<Membership>
        get() = viewClickSubject

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
            filteredData = results.values as List<Membership>
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            MembershipViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val member = filteredData[position]
        (holder as MembershipViewHolder).memberName.text = member.name
        val roleDescription = context.getString(member.roleNameRes)
        holder.memberDetails.text = context.getString(R.string.member_description_format, roleDescription,
                PhoneNumberFormatter.formatNumberForDisplay(member.phoneNumber, " "))
        RxView.clicks(holder.root).map { _ -> member }.subscribe(viewClickSubject)
    }

    // todo : add user profile images
    internal class MembershipViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.root
        var memberName = itemView.memberName
        var memberDetails = itemView.memberDetails
    }

}