package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_meeting_details.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.presenter.activity.MeetingDetailsPresenter
import za.org.grassroot2.util.DateFormatter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.adapter.PostAdapter
import za.org.grassroot2.view.dialog.AddMemberDialog
import za.org.grassroot2.view.dialog.OptionPickDialog
import za.org.grassroot2.view.fragment.GroupTasksFragment
import javax.inject.Inject

class MeetingDetailsActivity : GrassrootActivity(), MeetingDetailsPresenter.MeetingDetailsView {

    private var meetingUid: String? = null

    @Inject lateinit var presenter: MeetingDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions
    @Inject lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val triggeredByNotification = intent.getBooleanExtra(TRIGGERED_BY_NOTIFICATION, false)
        meetingUid = intent.getStringExtra(EXTRA_MEETING_UID)
        initView()
        presenter.attach(this)
        presenter.init(meetingUid!!, triggeredByNotification)
        meetingStatusText.setOnClickListener {
            val attendenceDialog = OptionPickDialog.attendenceChoiceDialog()
            disposables.add(attendenceDialog.clickAction().subscribe( { clickId ->
                attendenceDialog.dismiss()
                when (clickId) {
                    R.id.optionGoing -> presenter.respondToMeeting(meetingUid!!, Meeting.RSVP_YES)
                    R.id.optionMaybe -> presenter.respondToMeeting(meetingUid!!, Meeting.RSVP_MAYBE)
                    else -> presenter.respondToMeeting(meetingUid!!, Meeting.RSVP_NO)
                }
            }, {t -> t.printStackTrace() }))
            attendenceDialog.show(supportFragmentManager, "")
        }
        fab.setOnClickListener { writePost() }
        writePostButton.setOnClickListener { writePost() }
        posts.adapter = postAdapter
        posts.layoutManager = LinearLayoutManager(this)
    }

    private fun writePost() {
        CreatePostActivity.start(this, presenter.meeting.uid, presenter.meeting.parentUid)
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
        toolbar.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun render(meeting: Meeting) {
        meetingTitle.text = meeting.name
        meetingLocation.text = meeting.locationDescription
        meeting.deadlineMillis?.let { meetingDate.text = DateFormatter.formatMeetingDate(it) }
        renderDescription(meeting)
        renderResponseSection(meeting)
    }

    private fun renderResponseSection(meeting: Meeting) {
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
            optionGoing.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_YES) })
            optionMaybe.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_MAYBE) })
            optionNotGoing.setOnClickListener({ _ -> presenter.respondToMeeting(meeting.uid, Meeting.RSVP_NO) })
        }
    }

    private fun renderDescription(meeting: Meeting) {
        if (TextUtils.isEmpty(meeting.description)) {
            meetingDescription.visibility = View.GONE
        } else {
            meetingDescription.visibility = View.VISIBLE
            meetingDescription.text = meeting.description
        }
    }

    override fun renderPosts(posts: List<Post>) {
        if (posts.isNotEmpty()) {
            listTitle.visibility = View.VISIBLE
            postAdapter.setData(posts)
        }
    }

    companion object {

        val EXTRA_MEETING_UID = "group_uid"
        val TRIGGERED_BY_NOTIFICATION = "triggered_by_notification"
        private val REQUEST_PICK_CONTACTS = 1

        fun start(activity: Activity, meetingUid: String) {
            val intent = Intent(activity, MeetingDetailsActivity::class.java)
            intent.putExtra(EXTRA_MEETING_UID, meetingUid)
            activity.startActivity(intent)
        }
    }
}