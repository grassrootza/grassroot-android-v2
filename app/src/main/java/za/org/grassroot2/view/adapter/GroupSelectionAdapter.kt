package za.org.grassroot2.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.jakewharton.rxbinding2.view.RxView

import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.group_item_row.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.SelectableItem

/**
 * Created by luke on 2017/08/19.
 */

class GroupSelectionAdapter(private var data: List<SelectableItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewClickSubject = PublishSubject.create<Group>()

    val viewClickObservable: Observable<Group>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            SelectableGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.group_item_row, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        bindGroup(holder as SelectableGroupViewHolder, item)
    }

    private fun bindGroup(holder: SelectableGroupViewHolder, item: SelectableItem) {
        holder.name.text = item.name
        RxView.clicks(holder.name)
                .map { o -> item as Group }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateData(data: List<SelectableItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    internal class SelectableGroupViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.text
    }
}
