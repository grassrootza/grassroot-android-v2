package za.org.grassroot2.view.dialog

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
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
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class MultiOptionPickFragment : GrassrootFragment() {

    private val actionSubject = PublishSubject.create<Int>()
    private var options: HashMap<Int, ActionOption>? = null
    private lateinit var recyclerView: RecyclerView

    override fun onInject(activityComponent: ActivityComponent) {}

    override fun getLayoutResourceId(): Int  = R.layout.fragment_multi_option_pick

    fun clickAction(): Observable<Int> = actionSubject

    @OnClick(R.id.action_option_close)
    internal fun closeClick() {
        actionSubject.onComplete()
        activity.finish()
    }

    fun itemSelection(): PublishSubject<Int> {
        return actionSubject
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("in onCreateView about to do the thing")
        options = arguments.getSerializable(EXTRA_OPTIONS) as HashMap<Int, ActionOption>
        Timber.d("Thing done")
        initView()
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = action_option_list as RecyclerView
        setupAdapter()
    }

    private fun setupAdapter() {
        Timber.d("Setting up adapter, do we have a recycler view: %s", action_option_list)
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.item_divider))
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
        arguments.getSerializable(EXTRA_GROUP)?.let {
            val g = arguments.getSerializable(EXTRA_GROUP) as Group
            if (!GroupPermissionChecker.canCallMeeting(g)) {
                options!!.remove(R.id.callMeeting)
                Timber.d("Current user does not have callMeeting permissions")
            }
            if (!GroupPermissionChecker.canCreateTodo(g)) {
                options!!.remove(R.id.createTodo)
                Timber.d("Current user does not have createTodo permissions")
            }
            if (!GroupPermissionChecker.canCreateVote(g)) {
                options!!.remove(R.id.takeVote)
                Timber.d("Current user does not have takeVote permissions")
            }
        }
    }

    companion object {

        private val EXTRA_GROUP = "group"
        private val EXTRA_OPTIONS = "options"

        fun getActionPicker(group: Group?): MultiOptionPickFragment {
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictateMyAction, R.drawable.ic_mic_24dp))
            options.put(R.id.takeVote, ActionOption(R.id.takeVote, R.string.takeVote, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.createTodo, ActionOption(R.id.createTodo, R.string.createTodo, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.createGroup, ActionOption(R.id.createGroup, R.string.createGroup, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.callMeeting, ActionOption(R.id.callMeeting, R.string.callMeeting, R.drawable.ic_date_range_green_24dp))
            options.put(R.id.createLivewireAlert, ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_format_list_bulleted_24dp))
            val f = MultiOptionPickFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            b.putSerializable(EXTRA_GROUP, group)
            f.arguments = b
            return f
        }

        val homeActionPicker: MultiOptionPickFragment
            get() {
                Timber.d("In homeActionPicker within MultiOptionFragment")
                val options = LinkedHashMap<Int, ActionOption>()
                options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictateMyAction, R.drawable.ic_mic_24dp))
                options.put(R.id.takeVote, ActionOption(R.id.takeVote, R.string.takeVote, R.drawable.ic_format_list_bulleted_24dp))
                options.put(R.id.createTodo, ActionOption(R.id.createTodo, R.string.createTodo, R.drawable.ic_format_list_bulleted_24dp))
                options.put(R.id.createGroup, ActionOption(R.id.createGroup, R.string.createGroup, R.drawable.ic_format_list_bulleted_24dp))
                options.put(R.id.callMeeting, ActionOption(R.id.callMeeting, R.string.callMeeting, R.drawable.ic_date_range_green_24dp))
                options.put(R.id.createLivewireAlert, ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_format_list_bulleted_24dp))
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
                options.put(R.id.customOptions, ActionOption(R.id.customOptions, R.string.customOptions, R.drawable.ic_mic_24dp))
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
                options.put(R.id.todoAction, ActionOption(R.id.todoAction, R.string.todoAction, R.drawable.ic_mic_24dp))
                options.put(R.id.todoInformation, ActionOption(R.id.todoInformation, R.string.todoInformation, R.drawable.ic_mic_24dp))
                options.put(R.id.todoVolunteer, ActionOption(R.id.todoVolunteer, R.string.todoVolunteer, R.drawable.ic_mic_24dp))
                options.put(R.id.todoValidate, ActionOption(R.id.todoValidate, R.string.todoValidate, R.drawable.ic_mic_24dp))
                val f = MultiOptionPickFragment()
                val b = Bundle()
                b.putSerializable(EXTRA_OPTIONS, options)
                f.arguments = b
                Timber.e("todoOptionPicker construction complete, returning fragment")
                return f
            }

        fun homeTakeActionFragment(): MultiOptionPickFragment {
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictateMyAction, R.drawable.ic_mic_24dp))
            options.put(R.id.takeVote, ActionOption(R.id.takeVote, R.string.takeVote, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.createTodo, ActionOption(R.id.createTodo, R.string.createTodo, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.createGroup, ActionOption(R.id.createGroup, R.string.createGroup, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.callMeeting, ActionOption(R.id.callMeeting, R.string.callMeeting, R.drawable.ic_date_range_green_24dp))
            options.put(R.id.createLivewireAlert, ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_format_list_bulleted_24dp))
            val f = MultiOptionPickFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            f.arguments = b
            return f
        }

        // todo: think through
//        fun voteOptionFragment(vote: Vote): MultiOptionPickFragment {
//            val voteOptions = vote.voteOptions;
//            val options = LinkedHashMap<Int, ActionOption>()
//
//        }
    }
}