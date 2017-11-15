package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import com.github.florent37.viewtooltip.ViewTooltip
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_meeting_details.*
import kotlinx.android.synthetic.main.activity_meeting_details.view.*
import kotlinx.android.synthetic.main.marker_group.view.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.extensions.getColorCompat
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.presenter.GroupDetailsPresenter
import za.org.grassroot2.presenter.MeetingDetailsPresenter
import za.org.grassroot2.util.DateFormatter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.AddMemberDialog
import za.org.grassroot2.view.fragment.GroupTasksFragment
import java.util.ArrayList
import javax.inject.Inject

class MeetingDetailsActivity : GrassrootActivity(), MeetingDetailsPresenter.MeetingDetailsView {

    private var meetingUid: String? = null

    @Inject lateinit var presenter: MeetingDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        meetingUid = intent.getStringExtra(EXTRA_MEETING_UID)
        initView()
        presenter.attach(this)
        presenter.init(meetingUid!!)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_meeting_details

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    private fun displayInviteDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_PICK)
        df.setAddMemberDialogListener(object : AddMemberDialog.AddMemberDialogListener {
            override fun contactBook() {
                rxPermissions.request(Manifest.permission.READ_CONTACTS).subscribe({ result ->
                    if (result!!) {
                        PickContactActivity.startForResult(this@MeetingDetailsActivity, REQUEST_PICK_CONTACTS)
                    }
                }, { it.printStackTrace() })
            }

            override fun manual() {
                showFillDialog()
            }
        })
        df.show(supportFragmentManager, DIALOG_TAG)
    }

    private fun showFillDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_INSERT_MANUAL)
        df.show(supportFragmentManager, DIALOG_TAG)
    }

    private fun initView() {
        initTabs()
        initToolbar()
    }

    private fun initTabs() {
        val adapter = GenericViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GroupTasksFragment.newInstance(meetingUid, null), getString(R.string.title_all))
        adapter.addFragment(GroupTasksFragment.newInstance(meetingUid, GrassrootEntityType.VOTE), getString(R.string.title_votes))
        adapter.addFragment(GroupTasksFragment.newInstance(meetingUid, GrassrootEntityType.MEETING), getString(R.string.title_meetings))
        adapter.addFragment(GroupTasksFragment.newInstance(meetingUid, GrassrootEntityType.TODO), getString(R.string.title_todos))
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun emptyData() {
        ViewTooltip.on(fab)
                .color(getColorCompat(R.color.light_green))
                .corner(10).autoHide(true, 2000).padding(10, 10, 10, 10).align(ViewTooltip.ALIGN.CENTER)
                .position(ViewTooltip.Position.LEFT).text(R.string.info_group_create_item).setTextGravity(Gravity.CENTER)
                .show()
    }

    override fun render(meeting: Meeting) {
        meetingTitle.text = meeting.name
        meetingDescription.text = meeting.description
        meetingLocation.text = meeting.locationDescription
        meeting.deadlineMillis?.let { meetingDate.text = DateFormatter.formatMeetingDate(it) }
        if (meeting.hasResponded()) {
            meetingStatusText.visibility = View.VISIBLE
            when (meeting.response) {
                Meeting.RSVP_YES -> {
                    meetingStatusText.text = getString(R.string.going)
                    meetingStatusText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_attend, 0, 0)
                }
                Meeting.RSVP_MAYBE -> {
                    meetingStatusText.text = getString(R.string.maybe)
                    meetingStatusText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_maybe, 0, 0)
                }
                else -> {
                    meetingStatusText.text = getString(R.string.not_going)
                    meetingStatusText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_not_attending, 0, 0)
                }
            }
            optionContainer.visibility = View.GONE
        } else {
            optionGoing.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_YES)})
            optionMaybe.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_MAYBE)})
            optionNotGoing.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_NO)})
        }
    }

    companion object {

        private val EXTRA_MEETING_UID = "group_uid"
        private val REQUEST_PICK_CONTACTS = 1

        fun start(activity: Activity, meetingUid: String) {
            val intent = Intent(activity, MeetingDetailsActivity::class.java)
            intent.putExtra(EXTRA_MEETING_UID, meetingUid)
            activity.startActivity(intent)
        }
    }
}