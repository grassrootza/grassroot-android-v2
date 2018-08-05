package za.org.grassroot2.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.support.design.widget.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_action.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.GroupPermission
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.activity.CreateActionPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.MediaPickerFragment
import za.org.grassroot2.view.dialog.MultiOptionPickFragment
import za.org.grassroot2.view.fragment.*
import javax.inject.Inject

class CreateActionActivity : GrassrootActivity(), BackNavigationListener, CreateActionPresenter.CreateActionView {

    @Inject lateinit var presenter: CreateActionPresenter
    @Inject lateinit var rxPermission: RxPermissions
    @Inject lateinit var rxPermissions: RxPermissions

    private lateinit var adapter: GenericViewPagerAdapter
    private var created: Boolean = false
    private var shouldRemoveLast: Boolean = false

    override val layoutResourceId: Int
        get(): Int = R.layout.activity_create_action

    private fun nextStep() {
        val current = viewPager.currentItem
        if (current == adapter.count - 1) {
            finish()
        } else {
            viewPager!!.setCurrentItem(current + 1, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GenericViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        presenter.attach(this)
        if (intent.hasExtra(EXTRA_FROM_HOME)) {
            addHomeActionTypeFragment()
        } else {
            presenter.verifyGroupPermissions(intent.getStringExtra(EXTRA_GROUP_UID))
        }
    }

    private fun addHomeActionTypeFragment() {
        val createActionFragment = MultiOptionPickFragment.homeActionPicker(presenter.canCreateMeeting(),presenter.canCreateVote(),presenter.canCreateTodo())
        disposables.add(createActionFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.dictate -> {
                    RecordAudioActivity.start(this, REQUEST_DICTATE)
                }
                R.id.take_vote -> {
                    launchVoteSequence(null)
                }
                R.id.create_todo -> {
                    launchTodoSequence(null)
                }
                R.id.create_group -> {
                    launchGroupSequence()
                }
                R.id.call_meeting -> {
                    launchMeetingSequence(null)
                }
                R.id.create_livewire_alert -> {
                    removeAllViewsAboveCurrent()
                    presenter.initTask(CreateActionPresenter.ActionType.LivewireAlert)

                    addGroupSelectionFragment()

                    addHeadlineFragment()
                    addMediaFragment()
                    addLongDescriptionFragment()
                    nextStep()
                }
            }
        })
        adapter.addFragment(createActionFragment, "")
    }

    override fun proceedWithRender(group: Group?) {
        addActionFragment(MultiOptionPickFragment.getActionPicker(group), group)
    }

    override fun cameraForResult(contentProviderPath: String, s: String) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath))
        cameraIntent.putExtra("MY_UID", s)
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
    }

    override fun videoForResult(contentProviderPath: String, s: String) {
        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath))
        cameraIntent.putExtra("MY_UID", s)
        startActivityForResult(cameraIntent, REQUEST_RECORD_VIDEO)
    }

    override fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    private fun addTaskDateFragment(taskType: CreateActionPresenter.ActionType, task: GrassrootEntityType) {
        val taskDateFragment = MeetingDateFragment()
        disposables.add(taskDateFragment.meetingDatePicked().subscribe { date ->
            closeKeyboard()
            presenter.setTaskDate(taskType, date)
            val f = MeetingDateConfirmFragment.newInstance(date!!)
            disposables.add(f.meetingDateConfirmed().subscribe({ _ -> presenter.createTask(task) }, { it.printStackTrace() }))
            adapter.addFragment(f, "")
            nextStep()
            shouldRemoveLast = true
        })
        adapter.addFragment(taskDateFragment, "")
    }

    private fun addMeetingLocationFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.where_will_it_happen, R.string.info_meeting_location, R.string.hint_meeting_location, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { location ->
            presenter.setMeetingLocation(location)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addTodoSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_todo_description, R.string.hint_todo_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setTodoSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addVoteSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.vote_subject, R.string.info_vote_subject, R.string.hint_vote_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setVoteSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }


    private fun addMeetingSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_meeting_subject, R.string.hint_meeting_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setMeetingSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addHeadlineFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_headline, R.string.hint_headline, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { headline ->
            presenter.setHeadline(headline)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addLongDescriptionFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.long_description, R.string.info_long_description, R.string.hint_description, true)
        actionSingleInputFragment.isMultiLine = true
        disposables.add(actionSingleInputFragment.inputAdded().flatMapMaybe{ description ->
            presenter.setLongDescription(description)
            presenter.alertAndGroupName
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ entry ->
            val f = LivewireConfirmFragment.newInstance(entry.second, entry.first)
            adapter.addFragment(f, "")
            disposables.add(f.livewireAlertConfirmed().subscribe({ _ -> presenter.createAlert() }, { it.printStackTrace() }))
            nextStep()
            shouldRemoveLast = true
        }, { it.printStackTrace() }))
        adapter.addFragment(actionSingleInputFragment, "")
    }


    private fun addTodoTypeFragment() {
        val todoTypeFragment = MultiOptionPickFragment.todoOptionPicker
        disposables.add(todoTypeFragment.itemSelection().subscribe { integer ->
            when (integer) {
                R.id.todo_action -> {
                    presenter.setTodoType("ACTION_REQUIRED")
                }
                R.id.todo_information -> {
                    presenter.setTodoType("INFORMATION_REQUIRED")
                    addResponseTagFragment()
                }
                R.id.todo_volunteer -> {
                    presenter.setTodoType("VOLUNTEERS_REQUIRED")
                }
                R.id.todo_validate -> {
                    presenter.setTodoType("VALIDATION_REQUIRED")
                }
            }
            nextStep()
        })
        adapter.addFragment(todoTypeFragment, "")
    }

    private fun addVoteTypeFragment() {
        val voteOptionFragment = MultiOptionPickFragment.voteOptionPicker
        disposables.add(voteOptionFragment.itemSelection().subscribe { integer ->
            when (integer) {
                R.id.yes_no_option -> {
                    presenter.setVoteOptions(listOf("YES", "NO"))
                    addTaskDateFragment(CreateActionPresenter.ActionType.Vote, GrassrootEntityType.VOTE)
                    nextStep()
                }
                R.id.custom_options -> {
                    addVoteOptionsFragment()
                    addTaskDateFragment(CreateActionPresenter.ActionType.Vote, GrassrootEntityType.VOTE)
                    nextStep()
                }
            }
        })
        adapter.addFragment(voteOptionFragment, "")
    }

    private fun addVoteOptionsFragment() {
        val voteOptionsSingleInputFragment = VoteOptionsSingleInputFragment.newInstance(R.string.vote_option_header,  R.string.hint_vote_option, false)
        disposables.add(voteOptionsSingleInputFragment.inputAdded().subscribe { voteOptions ->
            presenter.setVoteOptions(voteOptions)
            nextStep()
        })
        adapter.addFragment(voteOptionsSingleInputFragment, "")
    }

    private fun addVoteDescriptionFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_vote_description, R.string.hint_description, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { description ->
            presenter.setVoteDescription(description)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addGroupSelectionFragment() {
        val fragment = GroupSelectionFragment()
        disposables.add(fragment.itemSelection().subscribe { group ->
            if (group.permissions.contains(GroupPermission.CREATE_GROUP_MEETING)) {
                presenter.setGroupUid(group)
                nextStep()
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.error_permission_denied, Snackbar.LENGTH_SHORT).show()
                finish()
            }
        })
        adapter.addFragment(fragment, "")
    }

    private fun addGroupNameFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.name_your_group , R.string.info_group_nature, R.string.hint_create_group_name, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { groupName ->
            presenter.setGroupName(groupName)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addResponseTagFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.enter_response_tag , R.string.info_response_tag, R.string.hint_response_tag, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { responseTag ->
            presenter.setTodoResponseTag(responseTag)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addGroupDescriptionFragment(groupUid: String) {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.describe_your_group , R.string.info_group_description, R.string.hint_description, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { groupDescription ->
            presenter.setGroupDescription(groupDescription)
            closeKeyboard()
            presenter.createGroup(groupUid)
            shouldRemoveLast = true
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun addActionFragment(fragment: MultiOptionPickFragment, group: Group?) {
        disposables.add(fragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.dictate -> {
                    RecordAudioActivity.start(this, REQUEST_DICTATE)
                }
                R.id.take_vote -> {
                    launchVoteSequence(group)
                }
                R.id.create_todo -> {
                    launchTodoSequence(group)
                }
                R.id.create_group -> {
                    launchGroupSequence()
                }
                R.id.call_meeting -> {
                    launchMeetingSequence(group)
                }
                R.id.create_livewire_alert -> {
                    removeAllViewsAboveCurrent()
                    presenter.initTask(CreateActionPresenter.ActionType.LivewireAlert)
                    if (group == null) {
                        addGroupSelectionFragment()
                    } else {
                        presenter.setGroupUid(group)
                    }
                    addHeadlineFragment()
                    addMediaFragment()
                    addLongDescriptionFragment()
                    nextStep()
                }
            }
        })
        adapter.addFragment(fragment, "")
    }

    private fun launchGroupSequence() {
        removeAllViewsAboveCurrent()
        var uid = presenter.initGroup()

        addGroupNameFragment()
        addGroupDescriptionFragment(uid)
        nextStep()
    }

    private fun launchTodoSequence(group: Group?) {
        removeAllViewsAboveCurrent()
        presenter.initTask(CreateActionPresenter.ActionType.Todo)
        if (group == null) {
            addGroupSelectionFragment()
        } else {
            presenter.setGroupUid(group)
        }

        addTodoSubjectFragment()
        addTodoTypeFragment()
        addTaskDateFragment(CreateActionPresenter.ActionType.Todo, GrassrootEntityType.TODO)
        nextStep()
    }

    private fun launchVoteSequence(group: Group?) {
        removeAllViewsAboveCurrent()
        presenter.initTask(CreateActionPresenter.ActionType.Vote)
        if (group == null) {
            addGroupSelectionFragment()
        } else {
            presenter.setGroupUid(group)
        }
        addVoteSubjectFragment()
        addVoteDescriptionFragment()
        addVoteTypeFragment()
        nextStep()
    }

    private fun launchMeetingSequence(group: Group?) {
        removeAllViewsAboveCurrent()
        presenter.initTask(CreateActionPresenter.ActionType.Meeting)
        if (group == null) {
            addGroupSelectionFragment()
        } else {
            presenter.setGroupUid(group)
        }

        addMeetingSubjectFragment()
        addMeetingLocationFragment()
        addTaskDateFragment(CreateActionPresenter.ActionType.Meeting, GrassrootEntityType.MEETING)
        nextStep()
    }

    private fun removeAllViewsAboveCurrent() {
        val current = viewPager!!.currentItem
        adapter.removeAllAbove(current)
    }

    private fun addMediaFragment() {
        val mediaPickerFragment = MediaPickerFragment.get()
        disposables.add(mediaPickerFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.photo -> presenter.takePhoto()
                R.id.video -> {
                    val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    if (takeVideoIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takeVideoIntent, REQUEST_RECORD_VIDEO)
                    }
                }
                R.id.audio -> RecordAudioActivity.start(this, REQUEST_RECORD_AUDIO)
                R.id.gallery -> presenter.pickFromGallery()
                R.id.skip -> nextStep()
            }
        })
        adapter.addFragment(mediaPickerFragment, "")
    }

    override fun backPressed() {
        val current = viewPager.currentItem
        if (shouldRemoveLast) {
            adapter.removeLast()
            shouldRemoveLast = false
        }
        if (current == 0 || created) {
            finish()
        } else {
            viewPager.setCurrentItem(current - 1, true)
        }
    }

    override fun backPressedAndRemoveLast() {
        shouldRemoveLast = false
        backPressed()
        adapter.removeLast()
    }

    override fun onBackPressed() {
        backPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun uploadSuccessfull(type: GrassrootEntityType) {
        created = true
        addSuccessFragment(type)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> presenter.cameraResult()
                REQUEST_RECORD_AUDIO -> presenter.handleAudio(data.data)
                REQUEST_RECORD_VIDEO -> presenter.handleVideo(data.data)
                REQUEST_DICTATE -> presenter.handleSpeech(data.data)
                else -> presenter.handlePickResult(data.data)
            }
            nextStep()
        }
    }

    override fun closeScreen() {
        finish()
    }

    private fun addSuccessFragment(type: GrassrootEntityType) {
        val f: ItemCreatedFragment = if (type == GrassrootEntityType.MEETING) {
            ItemCreatedFragment.get(presenter.task!!.parentUid, type)
        }
        else if (type == GrassrootEntityType.VOTE) {
            ItemCreatedFragment.get(presenter.task!!.parentUid, type)
        }
        else if (type == GrassrootEntityType.TODO) {
            ItemCreatedFragment.get(presenter.task!!.parentUid, type)
        }
        else if (type == GrassrootEntityType.GROUP) {
            ItemCreatedFragment.get(presenter.group!!.uid, type)
        }
        else {
            ItemCreatedFragment.get(presenter.alert!!.groupUid, type)
        }
        adapter.addFragment(f, "")
        nextStep()
    }

    companion object {

        private const val EXTRA_GROUP_UID = "groupUid"
        private const val EXTRA_FROM_HOME = "from_home"
        private const val EXTRA_START_ON_ACTION = "start_on_action"

        private const val REQUEST_TAKE_PHOTO = 1
        private const val REQUEST_RECORD_VIDEO = 2
        private const val REQUEST_GALLERY = 3
        private const val REQUEST_RECORD_AUDIO = 4
        private const val REQUEST_DICTATE = 5

        fun start(c: Context, groupUid: String?) {
            val i = Intent(c, CreateActionActivity::class.java)
            i.putExtra(EXTRA_GROUP_UID, groupUid)
            c.startActivity(i)
        }

        fun startFromHome(c: Context) {
            Timber.d("starting create action from home");
            val i = Intent(c, CreateActionActivity::class.java)
            i.putExtra(EXTRA_FROM_HOME, true)
            c.startActivity(i)
        }

        fun startOnAction(c: Context, action: Int, groupUid: String?) {
            val i = Intent(c, CreateActionActivity::class.java)
            i.putExtra(EXTRA_START_ON_ACTION, action)
            i.putExtra(groupUid, groupUid)
            c.startActivity(i)
        }

    }

}
