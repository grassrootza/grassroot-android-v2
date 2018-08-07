package za.org.grassroot2.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.jakewharton.rxbinding2.view.RxView

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.selectable_item_row.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.SelectableItem


/**
 * Created by luke on 2017/08/19.
 */

class ItemSelectionAdapter(private val data: List<SelectableItem>) : RecyclerView.Adapter<ItemSelectionAdapter.SelectableItemViewHolder>() {
    private val viewClickSubject = PublishSubject.create<String>()

    val viewClickObservable: Observable<String>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableItemViewHolder =
            SelectableItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.selectable_item_row, parent, false))

    override fun onBindViewHolder(holder: SelectableItemViewHolder, position: Int) {
        val item = data[position]

        holder.itemHeading.text = item.name
        holder.itemDescription.text = item.description
        holder.itemUid = item.uid

        // todo: disposableOnDetach "takeUntil" detaches
        RxView.clicks(holder.itemRoot)
                .map<String> { o -> holder.itemUid }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class SelectableItemViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var itemRoot: ViewGroup = itemView.item_root
        var itemHeading: TextView = itemView.item_heading
        var itemDescription: TextView = itemView.item_description
        var itemUid: String? = null

    }
}
