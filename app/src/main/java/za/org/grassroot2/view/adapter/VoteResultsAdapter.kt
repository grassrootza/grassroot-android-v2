package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_vote_result.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.task.VoteResult
import javax.inject.Inject

/**
 * Created by luke on 2018/01/16.
 */
class VoteResultsAdapter @Inject
constructor(private val context: Context, private var data: List<VoteResult>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            VoteResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_vote_result, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = data[position];
        val resultHolder = holder as VoteResultViewHolder;
        resultHolder.option.text = item.option;
        val countString = context.resources.getQuantityString(R.plurals.vote_count, item.voteCount) + " " + context.resources.getString(R.string.vote_percent, item.votePercent);
        resultHolder.option.text = countString;
    }

    fun setData(results: List<VoteResult>) {
        data = results
        notifyDataSetChanged()
    }

    internal class VoteResultViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var option: TextView = itemView.voteOptionLabel
        var count: TextView = itemView.voteOptionCount

    }
}