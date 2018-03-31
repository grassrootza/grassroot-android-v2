package za.org.grassroot2.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_todo_result.view.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.model.task.TodoResponse
import javax.inject.Inject

/**
 * Created by frtnx on 2018/03/28.
 */
class TodoResponseAdapter @Inject
constructor(private val context: Context, private var data: List<TodoResponse>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            TodoResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_todo_result, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = data[position]
        Timber.d("The contents of item.userName are: %s", item.userName)
        val resultHolder = holder as TodoResultViewHolder
        resultHolder.userName.text = item.userName
        resultHolder.responseText.text = item.todoResponse
    }

    fun setData(responses: List<TodoResponse>) {
        data = responses
        Timber.d("responses now inside adapter = %s", data)
        notifyDataSetChanged()
    }

    internal class TodoResultViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView = itemView.userDisplayName
        var responseText: TextView = itemView.responseSnippet

    }
}