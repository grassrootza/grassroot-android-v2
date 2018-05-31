package za.org.grassroot2.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_multi_option_pick.*
import kotlinx.android.synthetic.main.fragment_multi_option_pick.view.*
import kotlinx.android.synthetic.main.fragment_option_pick.view.*
import za.org.grassroot2.R
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.dto.ActionOption
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.rxbinding.RxTextView
import za.org.grassroot2.view.adapter.OptionAdapter

class OptionPickDialog : DialogFragment() {
    private val actionSubject = PublishSubject.create<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val v = LayoutInflater.from(activity).inflate(R.layout.fragment_option_pick, null, false)
        val options = arguments.getSerializable(EXTRA_OPTIONS) as HashMap<Int, ActionOption>
        setupAdapter(v, options)
        v.close.setOnClickListener {
            actionSubject.onComplete()
            dismiss()
        }
        builder.setView(v)
        val d = builder.create()
        d.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return d
    }

    fun clickAction(): Observable<Int> = actionSubject

    private fun setupAdapter(view: View, options: HashMap<Int, ActionOption>) {
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.item_divider))
        view.action_option_list.addItemDecoration(itemDecoration)
        view.action_option_list.layoutManager = LinearLayoutManager(activity)
        val adapter = OptionAdapter(ArrayList(options.values))
        adapter.viewClickObservable.subscribe(actionSubject)
        view.action_option_list.adapter = adapter
    }

    companion object {

        private val EXTRA_OPTIONS = "options"

        fun attendenceChoiceDialog(): OptionPickDialog {
            val dialog = OptionPickDialog()
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.optionGoing, ActionOption(R.id.optionGoing, R.string.going, R.drawable.ic_attend))
            options.put(R.id.optionMaybe, ActionOption(R.id.optionMaybe, R.string.maybe, R.drawable.ic_maybe))
            options.put(R.id.optionNotGoing, ActionOption(R.id.optionNotGoing, R.string.not_going, R.drawable.ic_not_attending))
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            dialog.arguments = b
            return dialog
        }
    }

}
