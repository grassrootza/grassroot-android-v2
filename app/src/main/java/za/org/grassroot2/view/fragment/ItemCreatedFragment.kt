package za.org.grassroot2.view.fragment

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_item_created.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.fragment.ItemCalledPresenter
import javax.inject.Inject

class ItemCreatedFragment : GrassrootFragment(), ItemCalledPresenter.MeetingCalledView {

    @Inject lateinit internal var presenter: ItemCalledPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        okButton.setOnClickListener { activity.finish() }
        presenter.loadGroupData(arguments.getString(EXTRA_GROUP_UID), arguments.getSerializable(EXTRA_ENTITY_TYPE) as GrassrootEntityType)
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_item_created

    override fun showDescription(memberCount: Int?, type: GrassrootEntityType) {
        when (type) {
            GrassrootEntityType.MEETING -> title.setText(R.string.meeting_called)
            GrassrootEntityType.POST -> title.setText(R.string.post_created)
            GrassrootEntityType.TODO -> title.setText(R.string.todo_created)
            GrassrootEntityType.VOTE -> title.setText(R.string.vote_created)
            GrassrootEntityType.LIVE_WIRE_ALERT -> title.setText(R.string.lwire_alert_sent)
        }
        text.text = resources.getQuantityString(R.plurals.notification_sent, memberCount!!, memberCount)
    }

    companion object {

        private val EXTRA_GROUP_UID = "group_uid"
        private val EXTRA_ENTITY_TYPE = "entity_type"

        operator fun get(groupUid: String, type: GrassrootEntityType): ItemCreatedFragment {
            val f = ItemCreatedFragment()
            val b = Bundle()
            b.putString(EXTRA_GROUP_UID, groupUid)
            b.putSerializable(EXTRA_ENTITY_TYPE, type)
            f.arguments = b
            return f
        }
    }
}
