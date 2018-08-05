package za.org.grassroot2.view.dialog

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_multi_option_pick.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.dto.ActionOption
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.view.adapter.OptionAdapter
import za.org.grassroot2.view.fragment.GrassrootFragment
import java.util.*
import kotlin.collections.LinkedHashMap

class MultiOptionPickFragment : GrassrootFragment() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_multi_option_pick

    private val actionSubject = PublishSubject.create<Int>()
    private var options: HashMap<Int, ActionOption>? = null
    private lateinit var recyclerView: RecyclerView

    override fun onInject(activityComponent: ActivityComponent) {}

    fun clickAction(): Observable<Int> = actionSubject

    @OnClick(R.id.action_option_close)
    internal fun closeClick() {
        actionSubject.onComplete()
        activity?.finish()
    }

    fun itemSelection(): PublishSubject<Int> {
        return actionSubject
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("in onCreateView about to do the thing")
        options = arguments!!.getSerializable(EXTRA_OPTIONS) as HashMap<Int, ActionOption>
        Timber.d("Thing done")
        initView()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = action_option_list as RecyclerView
        setupAdapter()
    }

    private fun setupAdapter() {
        Timber.d("Setting up adapter, do we have a recycler view: %s", action_option_list)
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(activity!!, R.drawable.item_divider)?.let { itemDecoration.setDrawable(it) }
        recyclerView.addItemDecoration(itemDecoration)
        val adapter = OptionAdapter(ArrayList(options!!.values))
        adapter.viewClickObservable.subscribe(actionSubject)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        Timber.e("and the list: %s", action_option_list);
        Timber.e("adapter set up, as follows: %s", recyclerView.adapter)
        Timber.e("adapter count: %s", action_option_list.adapter?.itemCount)
        adapter.notifyDataSetChanged()
        Timber.e("recycler view children: %s", recyclerView.layoutManager?.childCount)
    }

    private fun initView() {
        Timber.d("Initialising MultiOption view")
        val g = arguments!!.getSerializable(EXTRA_GROUP) as? Group
        g?.let { removeViewsForNoGroup(it) }

    }

    private fun removeViewsForNoGroup(g:Group) {
        if (!GroupPermissionChecker.canCallMeeting(g)) {
            options!!.remove(R.id.call_meeting)
            Timber.d("Current user does not have callMeeting permissions")
        }
        if (!GroupPermissionChecker.canCreateTodo(g)) {
            options!!.remove(R.id.create_todo)
            Timber.d("Current user does not have createTodo permissions")
        }
        if (!GroupPermissionChecker.canCreateVote(g)) {
            options!!.remove(R.id.take_vote)
            Timber.d("Current user does not have takeVote permissions")
        }
    }

    companion object {

        private val EXTRA_GROUP = "group"
        private val EXTRA_OPTIONS = "options"


        fun getActionPicker(group: Group?): MultiOptionPickFragment {
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictate_my_action, R.drawable.ic_mic_24dp))
            options.put(R.id.take_vote, ActionOption(R.id.take_vote, R.string.take_vote, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.create_todo, ActionOption(R.id.create_todo, R.string.create_todo, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.create_group, ActionOption(R.id.create_group, R.string.create_group, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.call_meeting, ActionOption(R.id.call_meeting, R.string.call_meeting, R.drawable.ic_date_range_green_24dp))
            options.put(R.id.create_livewire_alert, ActionOption(R.id.create_livewire_alert, R.string.create_livewire_alert, R.drawable.ic_format_list_bulleted_24dp))
            val f = MultiOptionPickFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            b.putSerializable(EXTRA_GROUP, group)
            f.arguments = b
            return f
        }


        fun homeActionPicker(canCreateMeeting:Boolean, canCreateVote:Boolean, canCreateTodo:Boolean): MultiOptionPickFragment {

            Timber.d("In homeActionPicker within MultiOptionFragment")
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictate_my_action, R.drawable.ic_mic_24dp))

             if(canCreateMeeting){
                 options.put(R.id.call_meeting, ActionOption(R.id.call_meeting, R.string.call_meeting, R.drawable.ic_date_range_green_24dp))
             }

             if(canCreateTodo){
                 options.put(R.id.create_todo, ActionOption(R.id.create_todo, R.string.create_todo, R.drawable.ic_format_list_bulleted_24dp))
             }

             if(canCreateVote){
                 options.put(R.id.take_vote, ActionOption(R.id.take_vote, R.string.take_vote, R.drawable.ic_format_list_bulleted_24dp))
             }

            options.put(R.id.create_livewire_alert, ActionOption(R.id.create_livewire_alert, R.string.create_livewire_alert, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.create_group, ActionOption(R.id.create_group, R.string.create_group, R.drawable.ic_format_list_bulleted_24dp))

            val f = MultiOptionPickFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            f.arguments = b
            Timber.d("Returning homeActionPicker fragment")
            return f

            }

        val voteOptionPicker: MultiOptionPickFragment
            get() {
                Timber.e("Inside voteOptionPicker in MultiOptionPickFragment")
                val options = LinkedHashMap<Int, ActionOption>()
                options.put(R.id.yes_no_option, ActionOption(R.id.yes_no_option, R.string.yes_no_option, R.drawable.ic_mic_24dp))
                options.put(R.id.custom_options, ActionOption(R.id.custom_options, R.string.custom_options, R.drawable.ic_mic_24dp))
                val f = MultiOptionPickFragment()
                val b = Bundle()
                b.putSerializable(EXTRA_OPTIONS, options)
                f.arguments = b
                Timber.e("Returning voteOptionPicker fragment")
                return f
            }

        val todoOptionPicker: MultiOptionPickFragment
            get() {
                Timber.e("Inside todoOptionPicker")
                val options = LinkedHashMap<Int, ActionOption>()
                options.put(R.id.todo_action, ActionOption(R.id.todo_action, R.string.todo_action, R.drawable.ic_mic_24dp))
                options.put(R.id.todo_information, ActionOption(R.id.todo_information, R.string.todo_information, R.drawable.ic_mic_24dp))
                options.put(R.id.todo_volunteer, ActionOption(R.id.todo_volunteer, R.string.todo_volunteer, R.drawable.ic_mic_24dp))
                options.put(R.id.todo_validate, ActionOption(R.id.todo_validate, R.string.todo_validate, R.drawable.ic_mic_24dp))
                val f = MultiOptionPickFragment()
                val b = Bundle()
                b.putSerializable(EXTRA_OPTIONS, options)
                f.arguments = b
                Timber.e("todoOptionPicker construction complete, returning fragment")
                return f
            }

    }
}