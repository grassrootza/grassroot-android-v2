package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.jakewharton.rxbinding2.view.RxView

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_group.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.Group
import za.org.grassroot2.util.LastModifiedFormatter

class GroupsAdapter(private val context: Context, data: List<Group>) : FooterEnabledAdapter<Group>(data) {
    private val viewClickSubject = PublishSubject.create<String>()

    val viewClickObservable: Observable<String>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            super.createFooterEnabledViewHolder(parent, viewType) ?: nonFooterViewHolder(parent)

    private fun nonFooterViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
            GroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false))

    override fun bindRegularViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder = vh as GroupViewHolder

        holder.name!!.text = item.name
        holder.count!!.text = holder.count!!.context.resources.getQuantityString(R.plurals.member_count, item.memberCount!!, item.memberCount)
        holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.lastTimeChangedServer)
        holder.organiser!!.text = "Placeholder"
        holder.letter!!.text = item.name.substring(0, 1)
        holder.letter!!.visibility = View.VISIBLE

        RxView.clicks(holder.root!!)
                .map { o -> item.uid }
                .subscribe(viewClickSubject)

    }

    internal class GroupViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var root: View? = itemView.root
        var letter: TextView? = itemView.letter
        var name: TextView? = itemView.name
        var organiser: TextView? = itemView.organiser
        var count: TextView? = itemView.count
        var lastModified: TextView? = itemView.lastModified

    }

}
