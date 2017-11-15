package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import java.util.HashMap

import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import za.org.grassroot2.R
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.rxbinding.RxView
import za.org.grassroot2.util.LastModifiedFormatter

class GroupTasksAdapter(private val context: Context, private var data: List<Task>?, private val olderTimestamp: Long) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewClickSubject = PublishSubject.create<Task>()

    val viewClickObservable: Observable<Task>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: RecyclerView.ViewHolder = when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false))
            VIEW_TYPE_TODO_NEW -> TodoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task_generic, parent, false))
            VIEW_TYPE_MEETING_NEW -> MeetingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task_generic, parent, false))
            VIEW_TYPE_OLD_POST -> OldPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task_older, parent, false))
            else -> VoteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_vote, parent, false))
        }
        return v
    }

    override fun getItemViewType(position: Int): Int {
        val task = data!![position]
        val type = task.type
        if (type == GrassrootEntityType.GROUP) {
            return VIEW_TYPE_HEADER
        }
        return when {
            task.deadlineMillis <= olderTimestamp -> VIEW_TYPE_OLD_POST
            else -> when (type) {
                GrassrootEntityType.MEETING -> VIEW_TYPE_MEETING_NEW
                GrassrootEntityType.TODO -> VIEW_TYPE_TODO_NEW
                else -> VIEW_TYPE_VOTE_NEW
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data!![position]
        val type = getItemViewType(position)
        when (type) {
            VIEW_TYPE_HEADER -> bindHeader(holder as HeaderViewHolder, item as HeaderItem)
            VIEW_TYPE_TODO_NEW -> bindTodo(holder as TodoViewHolder, item as Todo)
            VIEW_TYPE_VOTE_NEW -> bindVote(holder as VoteViewHolder, item as Vote)
            VIEW_TYPE_OLD_POST -> bindOldPost(holder as OldPostViewHolder, item)
            else -> bindMeeting(holder as MeetingViewHolder, item as Meeting)
        }
    }

    private fun bindOldPost(holder: OldPostViewHolder, item: Task) {
        holder.title!!.text = item.name
        holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        holder.subtitle!!.text = "happend"
    }

    private fun bindHeader(holder: HeaderViewHolder, item: HeaderItem) {
        holder.title!!.text = item.name
    }

    private fun bindMeeting(holder: MeetingViewHolder, item: Meeting) {
        holder.title!!.text = context.getString(R.string.text_meeting_title, item.callerName, item.name)
        holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        holder.option1!!.setText(R.string.text_rsvp)
        holder.option2!!.setText(R.string.text_view)
        setupClick(holder.root, item)
    }

    private fun bindVote(holder: VoteViewHolder, item: Vote) {
        holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        holder.title!!.text = context.getString(R.string.text_vote_title, item.callerName, item.name)
        renderOptions(item.voteOptions, holder)
        setupClick(holder.root, item)
    }

    private fun renderOptions(options: HashMap<String, Int>, holder: VoteViewHolder) {
        if (holder.voteOptionsContainer!!.childCount > 1) {
            holder.voteOptionsContainer!!.removeViews(0, holder.voteOptionsContainer!!.childCount - 1)
        }
        val total = options.values.sum()
        var viewPosition = 0
        for ((key, value) in options) {
            val option = LayoutInflater.from(context).inflate(R.layout.vote_option, holder.voteOptionsContainer, false)
            val progress = option.findViewById(R.id.progress) as ProgressBar
            progress.max = total
            progress.progress = value
            val title = option.findViewById(R.id.title) as TextView
            title.text = key
            val count = option.findViewById(R.id.count) as TextView
            count.text = context.resources.getQuantityString(R.plurals.vote_count, value, value)
            holder.voteOptionsContainer!!.addView(option, viewPosition++)
        }
    }

    private fun bindTodo(holder: TodoViewHolder, item: Todo) {
        holder.title!!.text = context.getString(R.string.text_todo_title, item.recorderName, item.name)
        holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.deadlineMillis)
        setupClick(holder.root, item)
    }

    private fun setupClick(view: View?, item: Task) {
        RxView.clicks(view!!)
                .map { _ -> item }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int = data!!.size

    fun setData(tasks: MutableList<Task>) {
        var index = -1
        for (i in tasks.indices) {
            val t = tasks[i]
            if (t.deadlineMillis <= olderTimestamp) {
                index = i
                break
            }
        }
        if (index == 0) {
            tasks.add(index, HeaderItem(context.getString(R.string.text_old_header)))
        } else {
            if (index > 0) {
                tasks.add(index, HeaderItem(context.getString(R.string.text_old_header)))
            }
            tasks.add(0, HeaderItem(context.getString(R.string.text_group_header)))
        }
        data = tasks
        notifyDataSetChanged()
    }

    internal class VoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.root)
        @JvmField
        var root: View? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.modified)
        @JvmField
        var lastModified: TextView? = null
        @BindView(R.id.voteButton)
        @JvmField
        var vote: TextView? = null
        @BindView(R.id.voteTotalCount)
        @JvmField
        var voteTotalCount: TextView? = null
        @BindView(R.id.voteOptionsContainer)
        @JvmField
        var voteOptionsContainer: LinearLayout? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    internal class OldPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.root)
        @JvmField
        var root: View? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.subtitle)
        @JvmField
        var subtitle: TextView? = null
        @BindView(R.id.modified)
        @JvmField
        var lastModified: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    internal class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.root)
        @JvmField
        var root: View? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.modified)
        @JvmField
        var lastModified: TextView? = null
        @BindView(R.id.option1)
        @JvmField
        var option1: TextView? = null
        @BindView(R.id.option2)
        @JvmField
        var option2: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    internal class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.root)
        @JvmField
        var root: View? = null
        @BindView(R.id.image)
        @JvmField
        var image: ImageView? = null
        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null
        @BindView(R.id.modified)
        @JvmField
        var lastModified: TextView? = null
        @BindView(R.id.option1)
        @JvmField
        var option1: TextView? = null
        @BindView(R.id.option2)
        @JvmField
        var option2: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.title)
        @JvmField
        var title: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    companion object {

        private val VIEW_TYPE_TODO_NEW = 1
        private val VIEW_TYPE_VOTE_NEW = 2
        private val VIEW_TYPE_MEETING_NEW = 3
        private val VIEW_TYPE_HEADER = 4
        private val VIEW_TYPE_OLD_POST = 5
    }

}
