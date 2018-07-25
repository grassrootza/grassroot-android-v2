package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.item_contact.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.contact.Contact
import java.util.*

class ContactsAdapter(private val context: Context, private var data: List<Contact>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedItems = SparseBooleanArray()

    fun update(data: List<Contact>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun toggleSelection(pos: Int) {
        if (selectedItems.get(pos)) {
            selectedItems.delete(pos)
        } else {
            selectedItems.put(pos, true)
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Long> {
        val result = ArrayList<Long>()
        for (i in 0 until selectedItems.size()) {
            result.add(data!![selectedItems.keyAt(i)].id)
        }
        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data!![position]
        (holder as ContactViewHolder).bind(item, position)
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    internal inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var root: View = view.root
        var title: TextView = view.title
        var number: TextView = view.number

        fun bind(c: Contact, position: Int) {
            title.text = c.displayName
            if (!c.phoneNumbers.isEmpty()) number.text = c.phoneNumbers[0]
            handleSelection(position)
            root.setOnClickListener { v -> toggleSelection(position) }
        }

        private fun handleSelection(position: Int) =
                if (selectedItems.get(position, false))
                    root.setBackgroundColor(context.resources.getColor(R.color.colorPrimaryTransparent))
                else
                    root.setBackgroundColor(0)
    }
}
