package za.org.grassroot2.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import java.util.ArrayList

abstract class FooterEnabledAdapter<E>(data: List<E>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var items: List<E> = ArrayList()
    internal var footers: MutableList<View> = ArrayList()

    init {
        items = data
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): RecyclerView.ViewHolder {
        if (type == TYPE_FOOTER) {
            val frameLayout = FrameLayout(viewGroup.context)
            //make sure it fills the space
            frameLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return FooterViewHolder(frameLayout)
        }
        return null
    }

    protected abstract fun bindRegularViewHolder(vh: RecyclerView.ViewHolder, position: Int)

    protected fun prepareFooter(vh: FooterViewHolder, v: View) {
        vh.base.removeAllViews()
        vh.base.addView(v)
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        if (position >= items.size) {
            val v = footers[position - items.size]
            prepareFooter(vh as FooterViewHolder, v)
        } else {
            bindRegularViewHolder(vh, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size + footers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= items.size) {
            TYPE_FOOTER
        } else TYPE_ITEM
    }

    fun addFooter(footer: View) {
        if (!footers.contains(footer)) {
            footers.add(footer)
            notifyItemInserted(items.size + footers.size - 1)
        }
    }

    fun removeFooter(footer: View) {
        if (footers.contains(footer)) {
            notifyItemRemoved(items.size + footers.indexOf(footer))
            footers.remove(footer)
            if (footer.parent != null) {
                (footer.parent as ViewGroup).removeView(footer)
            }
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var base: FrameLayout

        init {
            this.base = itemView as FrameLayout
        }
    }

    companion object {

        val TYPE_FOOTER = 222
        val TYPE_ITEM = 333
    }
}