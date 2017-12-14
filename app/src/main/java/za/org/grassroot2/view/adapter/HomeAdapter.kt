package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_home.view.*
import za.org.grassroot2.R
import za.org.grassroot2.extensions.getColorCompat
import za.org.grassroot2.extensions.getHtml
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.rxbinding.RxView
import za.org.grassroot2.util.LastModifiedFormatter
import javax.inject.Inject

class HomeAdapter @Inject
constructor(private val context: Context, private var data: List<HomeFeedItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredData : List<HomeFeedItem> = data.toList()

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            val filtered = data.filter { homeFeedItem -> homeFeedItem.searchableContent().contains(constraint, true) }
            results.values = filtered
            results.count = filtered.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filteredData = results.values as List<HomeFeedItem>
            notifyDataSetChanged()
        }
    }

    private val viewClickSubject = PublishSubject.create<HomeFeedItem>()

    val viewClickObservable: Observable<HomeFeedItem>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                VIEW_TYPE_TODO -> HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))
                VIEW_TYPE_MEETING -> HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))
                VIEW_TYPE_VOTE -> HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))
                else -> HomeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))
            }

    override fun getItemViewType(position: Int): Int {
        val task = filteredData[position]
        val type = when (task) {
            is Task -> task.type
            is AroundEntity -> GrassrootEntityType.PUBLIC_MEETING
            else -> GrassrootEntityType.LIVE_WIRE_ALERT
        }
        return when (type) {
            GrassrootEntityType.MEETING -> VIEW_TYPE_MEETING
            GrassrootEntityType.TODO -> VIEW_TYPE_TODO
            GrassrootEntityType.LIVE_WIRE_ALERT -> VIEW_TYPE_ALERT
            GrassrootEntityType.PUBLIC_MEETING -> VIEW_TYPE_PUBLIC_MEETNG
            else -> VIEW_TYPE_VOTE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = filteredData[position]
        val type = getItemViewType(position)
        when (type) {
            VIEW_TYPE_ALERT -> bindAlert(holder as HomeViewHolder, item as LiveWireAlert)
            VIEW_TYPE_TODO -> bindTodo(holder as HomeViewHolder, item as Todo)
            VIEW_TYPE_VOTE -> bindVote(holder as HomeViewHolder, item as Vote)
            VIEW_TYPE_PUBLIC_MEETNG -> bindPublicMeeting(holder as HomeViewHolder, item as AroundEntity)
            else -> bindMeeting(holder as HomeViewHolder, item as Meeting)
        }
    }

    private fun bindPublicMeeting(holder: HomeViewHolder, item: AroundEntity) {
        holder.header.text = item.ancestorGroupName
        holder.header.setBackgroundResource(R.drawable.bg_blue_rounded_stroke)
        holder.header.setTextColor(context.getColorCompat(R.color.light_blue))
        holder.image.setImageResource(R.drawable.ic_nearyou)
        holder.title.text = context.getHtml(R.string.home_quote_subtitle, item.title)
        holder.subtitle.text = context.getString(R.string.happenig_now)
        holder.options.visibility = View.GONE
    }

    private fun bindAlert(holder: HomeViewHolder, item: LiveWireAlert) {
        holder.header.text = item.ancestorGroupName
        setBackgroundSolidRounded(holder)
        holder.image.setImageResource(R.drawable.ic_alert)
        holder.title.text = context.getHtml(R.string.home_alert_title, item.creatingUserName)
        holder.subtitle.text = context.getHtml(R.string.home_quote_subtitle, item.headline)
        holder.header.setTextColor(context.getColorCompat(R.color.white))
        holder.lastModified.text = LastModifiedFormatter.lastSeen(context, item.createdDate())
        holder.options.visibility = View.GONE
    }

    private fun bindMeeting(holder: HomeViewHolder, item: Meeting) {
        setBackground(holder, item)
        holder.image.setImageResource(R.drawable.ic_event)
        holder.title.text = context.getHtml(R.string.home_meeting_title, item.callerName)
        holder.subtitle.text = context.getHtml(R.string.home_quote_subtitle, item.name)
        holder.header.text = item.ancestorGroupName
        holder.lastModified.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        setBackgroundSolidRounded(holder)
        setupClick(holder.root, item)
        holder.options.visibility = View.GONE
    }

    private fun setBackgroundSolidRounded(holder: HomeViewHolder) {
        holder.header.setBackgroundResource(R.drawable.bg_blue_rounded_solid)
        holder.header.setTextColor(context.getColorCompat(R.color.white))
    }

    private fun bindVote(holder: HomeViewHolder, item: Vote) {
        setBackground(holder, item)
        holder.image.setImageResource(R.drawable.ic_vote)
        holder.header.setBackgroundResource(R.drawable.bg_blue_rounded_solid)
        holder.header.text = item.ancestorGroupName
        holder.header.setTextColor(context.getColorCompat(R.color.white))
        holder.options.visibility = View.GONE
        holder.title.text = context.getHtml(R.string.home_vote_title, item.callerName)
        holder.lastModified.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        holder.subtitle.text = item.name
        setupClick(holder.root, item)
    }

    private fun setBackground(holder: HomeViewHolder, item: Task) {
        holder.root.setBackgroundColor(if (item.deadlineMillis > System.currentTimeMillis()) {
            context.getColorCompat(R.color.light_green2)
        } else {
            context.getColorCompat(R.color.white)
        })
    }

    private fun bindTodo(holder: HomeViewHolder, item: Todo) {
        setBackground(holder, item)
        holder.image.setImageResource(R.drawable.ic_todo)
        holder.header.setBackgroundResource(R.drawable.bg_blue_rounded_solid)
        holder.header.text = item.ancestorGroupName
        holder.header.setTextColor(context.getColorCompat(R.color.white))
        holder.options.visibility = View.GONE
        holder.title.text = context.getHtml(R.string.home_text_todo_title, item.recorderName)
        holder.subtitle.text = context.getHtml(R.string.home_quote_subtitle, item.name)
        holder.lastModified.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        setupClick(holder.root, item)
    }

    private fun setupClick(view: View?, feedItem: HomeFeedItem) {
        RxView.clicks(view!!)
                .map { _ -> feedItem }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int = filteredData.size

    fun setData(tasks: List<HomeFeedItem>) {
        data = tasks
        filteredData = tasks.toList()
        notifyDataSetChanged()
    }

    internal class HomeViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var root: View = itemView.root
        var image: ImageView = itemView.image
        var header: TextView = itemView.header
        var title: TextView = itemView.title
        var subtitle: TextView = itemView.subtitle
        var options: View = itemView.options
        var lastModified: TextView = itemView.modified

    }

    companion object {
        private val VIEW_TYPE_TODO = 1
        private val VIEW_TYPE_VOTE = 2
        private val VIEW_TYPE_MEETING = 3
        private val VIEW_TYPE_ALERT = 4
        private val VIEW_TYPE_PUBLIC_MEETNG = 5
    }

}
