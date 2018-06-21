package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_post.view.*
import za.org.grassroot2.R
import za.org.grassroot2.extensions.getHtml
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.Post
import za.org.grassroot2.util.LastModifiedFormatter
import javax.inject.Inject

class PostAdapter @Inject constructor(private val context: Context, private var data: List<Post>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewClickSubject = PublishSubject.create<HomeFeedItem>()

    val viewClickObservable: Observable<HomeFeedItem>
        get() = viewClickSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        bindPost(holder as PostViewHolder, item)
    }

    private fun bindPost(holder: PostViewHolder, item: Post) {
        holder.title.text = context.getHtml(R.string.post_title, item.userDisplayName)
        holder.subtitle.text = context.getHtml(R.string.home_quote_subtitle, item.caption)
        holder.lastModified.text = LastModifiedFormatter.lastSeen(context, item.creationTime)
    }

    private fun setupClick(view: View?, feedItem: HomeFeedItem) {
        RxView.clicks(view!!)
                .map { _ -> feedItem }
                .subscribe(viewClickSubject)
    }

    override fun getItemCount(): Int = data.size

    fun setData(posts: List<Post>) {
        data = posts
        notifyDataSetChanged()
    }

    internal class PostViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userImage: ImageView = itemView.userImage
        var title: TextView = itemView.title
        var subtitle: TextView = itemView.subtitle
        var lastModified: TextView = itemView.modified
        var postImage: ImageView = itemView.postImage

    }

}
