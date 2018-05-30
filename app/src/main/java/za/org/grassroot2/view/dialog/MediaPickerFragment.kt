package za.org.grassroot2.view.dialog

import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_pick_media.*
import kotlinx.android.synthetic.main.fragment_pick_media.view.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.dto.ActionOption
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.rxbinding.RxView
import za.org.grassroot2.view.adapter.OptionAdapter
import za.org.grassroot2.view.fragment.GrassrootFragment
import java.util.*

class MediaPickerFragment : GrassrootFragment() {

    private val actionSubject = PublishSubject.create<Int>()
    private var options: HashMap<Int, ActionOption>? = null

    override fun onInject(activityComponent: ActivityComponent) {}

    override fun getLayoutResourceId(): Int = R.layout.fragment_pick_media

    fun clickAction(): Observable<Int> = actionSubject

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        options = arguments.getSerializable(EXTRA_OPTIONS) as HashMap<Int, ActionOption>
        initView()
        setupAdapter(v!!)
        if (arguments.getBoolean(EXTRA_SKIP_BUTTON_ENABLED, true)) {
            v.skip.visibility = View.GONE
        } else {
            RxView.clicks(v.skip!!).map { v.skip!!.id }.subscribe(actionSubject)
        }
        return v
    }

    private fun setupAdapter(v: View) {
        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.item_divider))
        v.list!!.addItemDecoration(itemDecoration)
        v.list!!.layoutManager = LinearLayoutManager(activity)
        val adapter = OptionAdapter(ArrayList(options!!.values))
        adapter.viewClickObservable.subscribe(actionSubject)
        v.list!!.adapter = adapter
    }

    private fun initView() {
        val g = arguments.getSerializable(EXTRA_GROUP) as Group?
        g?.let {
            if (!GroupPermissionChecker.canCallMeeting(g)) {
                options!!.remove(R.id.call_meeting)
            }
            if (!GroupPermissionChecker.canCreateTodo(g)) {
                options!!.remove(R.id.create_todo)
            }
            if (!GroupPermissionChecker.canCreateVote(g)) {
                options!!.remove(R.id.take_vote)
            }
        }
    }

    companion object {

        private val EXTRA_GROUP = "group"
        private val EXTRA_OPTIONS = "options"
        private val EXTRA_SKIP_BUTTON_ENABLED = "skip"

        fun get(skipEnabled: Boolean = true): MediaPickerFragment{
            val options = LinkedHashMap<Int, ActionOption>()
            options.put(R.id.photo, ActionOption(R.id.photo, R.string.take_photo, 0))
            options.put(R.id.gallery, ActionOption(R.id.gallery, R.string.pick_gallery, 0))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                options.put(R.id.audio, ActionOption(R.id.audio, R.string.take_audio, 0))
            }
            options.put(R.id.video, ActionOption(R.id.video, R.string.take_video, 0))
            val f = MediaPickerFragment()
            val b = Bundle()
            b.putSerializable(EXTRA_OPTIONS, options)
            b.putBoolean(EXTRA_SKIP_BUTTON_ENABLED, skipEnabled)
            f.arguments = b
            return f
        }
    }


}