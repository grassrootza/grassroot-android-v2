package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import za.org.grassroot2.R
import za.org.grassroot2.model.Group
import za.org.grassroot2.util.LastModifiedFormatter

class GroupsAdapter(private val context: Context, data: List<Group>) : FooterEnabledAdapter<Group>(data) {
    private val viewClickSubject = PublishSubject.create<String>()


    private val groupImageClickSubject = PublishSubject.create<String>()

    val viewClickObservable: Observable<String>
        get() = viewClickSubject

    val groupImageClickObservable: Observable<String>
        get() = groupImageClickSubject


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            super.createFooterEnabledViewHolder(parent, viewType) ?: nonFooterViewHolder(parent)

    private fun nonFooterViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
            GroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false))

    override fun bindRegularViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder = vh as GroupViewHolder


        if (item != null) {
            holder.name!!.text = item.name
            holder.count!!.text = holder.count!!.context.resources.getQuantityString(R.plurals.member_count, item.memberCount!!, item.memberCount)
            holder.lastModified!!.text = LastModifiedFormatter.lastSeen(context, item.lastTimeChangedServer)
            holder.organiser!!.text = "Placeholder"
            holder.letter!!.text = item?.name.substring(0, 1)
            holder.letter!!.visibility = View.VISIBLE

            if (item.profileImageUrl != null) {
                Picasso.get()
                        .load(item.profileImageUrl)
                        .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                        .centerCrop()
                        .into(holder.image)
            }

            RxView.clicks(holder.root!!)
                    .map { o -> item.uid }
                    .subscribe(viewClickSubject)

            RxView.clicks(holder.image!!)
                    .map { `object` -> item.uid }
                    .subscribe(groupImageClickSubject)

        }
    }

    fun setImage(imageUrl: String, groupUid: String) {
        for (group in items) {
            if (group.uid == groupUid) {
                group.profileImageUrl = imageUrl
                notifyItemChanged(items.indexOf(group))
            }
        }
    }

    internal class GroupViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @JvmField
        @BindView(R.id.root)
        var root: View? = null

        @JvmField
        @BindView(R.id.letter)
        var letter: TextView? = null

        @JvmField
        @BindView(R.id.name)
        var name: TextView? = null

        @JvmField
        @BindView(R.id.organiser)
        var organiser: TextView? = null

        @JvmField
        @BindView(R.id.count)
        var count: TextView? = null

        @JvmField
        @BindView(R.id.lastModified)
        var lastModified: TextView? = null

        @JvmField
        @BindView(R.id.image)
        var image: ImageView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

    }

}
