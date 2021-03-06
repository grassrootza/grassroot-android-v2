package za.org.grassroot2.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_option.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.dto.ActionOption

/**
 * Created by qbasso on 25.10.2017.
 */
class OptionAdapter(private val data: List<ActionOption>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewClickSubject = PublishSubject.create<Int>()

    val viewClickObservable: Observable<Int>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = OptionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false));

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        bindHolder(holder as OptionViewHolder, item);
    }

    private fun bindHolder(holder: OptionViewHolder, option: ActionOption) {
        holder.text.setText(option.textId)
        holder.text.setCompoundDrawablesWithIntrinsicBounds(option.resId, 0, 0, 0)
        RxView.clicks(holder.text)
                .map { o -> option.id }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal class OptionViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.text
    }
}
