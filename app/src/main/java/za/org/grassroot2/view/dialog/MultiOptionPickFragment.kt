package za.org.grassroot2.view.dialog

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_multi_option_pick.*
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

    private val actionSubject = PublishSubject.create<Int>()
    private var options: HashMap<Int, ActionOption>? = null

    override fun onInject(activityComponent: ActivityComponent) {}

    override fun getLayoutResourceId(): Int  = R.layout.fragment_multi_option_pick

    fun clickAction(): Observable<Int> = actionSubject

    @OnClick(R.id.close)
    internal fun closeClick() {
        actionSubject.onComplete()
        activity?.finish()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        options = arguments!!.getSerializable(EXTRA_OPTIONS) as HashMap<Int, ActionOption>
        initView()
        setupAdapter()
        return v
    }

    private fun setupAdapter() {
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(activity!!, R.drawable.item_divider)?.let { itemDecoration.setDrawable(it) }
        list?.addItemDecoration(itemDecoration)
        list?.layoutManager = LinearLayoutManager(activity)
        val adapter = OptionAdapter(ArrayList(options!!.values))
        adapter.viewClickObservable.subscribe(actionSubject)
        list?.adapter = adapter
    }

    private fun initView() {
        arguments?.getSerializable(EXTRA_GROUP)?.let {
            val g = arguments?.getSerializable(EXTRA_GROUP) as Group
            if (!GroupPermissionChecker.canCallMeeting(g)) {
                options!!.remove(R.id.callMeeting)
            }
            if (!GroupPermissionChecker.canCreateTodo(g)) {
                options!!.remove(R.id.createTodo)
            }
            if (!GroupPermissionChecker.canCreateVote(g)) {
                options!!.remove(R.id.takeVote)
            }
        }
    }

    companion object {

        private val EXTRA_GROUP = "group"
        private val EXTRA_OPTIONS = "options"

        fun getActionPicker(group: Group?): MultiOptionPickFragment {
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictate_your_action, R.drawable.ic_mic_24dp))
            options.put(R.id.createLivewireAlert, ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_mic_24dp))
            options.put(R.id.createTodo, ActionOption(R.id.createTodo, R.string.create_to_do, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.takeVote, ActionOption(R.id.takeVote, R.string.take_a_vote, R.drawable.ic_mic_24dp))
            options.put(R.id.callMeeting, ActionOption(R.id.callMeeting, R.string.call_a_meeting, R.drawable.ic_date_range_green_24dp))
            val f = MultiOptionPickFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            b.putSerializable(EXTRA_GROUP, group)
            f.arguments = b
            return f
        }

        val homeActionPicker: MultiOptionPickFragment
            get() {
                val options = LinkedHashMap<Int, ActionOption>()
                options.put(R.id.createGroup, ActionOption(R.id.createGroup, R.string.createGroup, R.drawable.ic_mic_24dp))
                options.put(R.id.takeAction, ActionOption(R.id.takeAction, R.string.takeAction, R.drawable.ic_mic_24dp))
                options.put(R.id.dictate, ActionOption(R.id.dictate, R.string.dictateMyAction, R.drawable.ic_date_range_green_24dp))
                val f = MultiOptionPickFragment()
                val b = Bundle()
                b.putSerializable(EXTRA_OPTIONS, options)
                f.arguments = b
                return f
            }

        fun homeTakeActionFragment(): MultiOptionPickFragment {
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.createLivewireAlert, ActionOption(R.id.createLivewireAlert, R.string.create_livewire_alert, R.drawable.ic_mic_24dp))
            options.put(R.id.createTodo, ActionOption(R.id.createTodo, R.string.create_to_do, R.drawable.ic_format_list_bulleted_24dp))
            options.put(R.id.takeVote, ActionOption(R.id.takeVote, R.string.take_a_vote, R.drawable.ic_mic_24dp))
            options.put(R.id.callMeeting, ActionOption(R.id.callMeeting, R.string.call_a_meeting, R.drawable.ic_date_range_green_24dp))
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