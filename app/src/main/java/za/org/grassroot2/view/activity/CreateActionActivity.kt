package za.org.grassroot2.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.support.design.widget.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_action.*
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.activity_create_meeting.*
import kotlinx.android.synthetic.main.activity_create_todo.*
import kotlinx.android.synthetic.main.activity_create_vote.*
import za.org.grassroot2.R
import com.tbruyelle.rxpermissions2.RxPermissions
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.GroupPermission
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.presenter.activity.CreateActionPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.MediaPickerFragment
import za.org.grassroot2.view.dialog.MultiOptionPickFragment
//import za.org.grassroot2.view.activity.PickContactActivity
import za.org.grassroot2.view.dialog.AddMemberDialog
import za.org.grassroot2.view.fragment.*
import javax.inject.Inject

class CreateActionActivity : GrassrootActivity(), BackNavigationListener, CreateActionPresenter.CreateActionView {

    @Inject lateinit var presenter: CreateActionPresenter
    @Inject lateinit var rxPermission: RxPermissions
    //@Inject lateinit var contactPicker: PickContactActivity
    @Inject lateinit var rxPermissions: RxPermissions

    private lateinit var adapter: GenericViewPagerAdapter
    private var created: Boolean = false
    private var shouldRemoveLast: Boolean = false

    override fun getLayoutResourceId(): Int = R.layout.activity_create_action

    private fun nextStep() {
        val current = viewPager.currentItem
        if (current == adapter.count - 1) {
            finish()
        } else {
            viewPager!!.setCurrentItem(current + 1, true)
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
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
        val createActionFragment = MultiOptionPickFragment.homeActionPicker
        disposables.add(createActionFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.dictate -> {
                    RecordAudioActivity.start(this, REQUEST_DICTATE)
                }
                R.id.takeVote -> {
                    launchVoteSequence(null)
                }
                R.id.createTodo -> {
                    launchTodoSequence(null)
                }
                R.id.createGroup -> {
                    launchGroupSequence()
                }
                R.id.callMeeting -> {
                    launchMeetingSequence(null)
                }
                R.id.createLivewireAlert -> {
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

    // Todo
    private fun addTaskDateFragment(task: GrassrootEntityType) {
        val taskDateFragment = MeetingDateFragment()
        disposables.add(taskDateFragment.meetingDatePicked().subscribe { date ->
            closeKeyboard()
            presenter.setMeetingDate(date)
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
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_vote_subject, R.string.hint_vote_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setVoteSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    /*
    private fun addMeetingSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_meeting_subject, R.string.hint_meeting_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setMeetingSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }
    */

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
        // TODO: Input fields vary by type
    }

    private fun addVoteOptionsFragment() {
        // TODO: Design a layout
        // NOTE: Must have dynamic input fields. Cater for custom vote options
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

    private fun addGroupNameDescriptionFragment() {
        val group = Group()
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.name_your_group , R.string.info_group_nature, R.string.hint_create_group_name, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { groupName ->
            group.setName(groupName)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")

        val sActionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.describe_your_group , R.string.info_group_description, R.string.hint_description, false)
        disposables.add(sActionSingleInputFragment.inputAdded().subscribe { groupDescription ->
            group.setDescription(groupDescription)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")

        presenter.createGroup(group.name, group.description)
    }

    /*
    private fun addGroupDescriptionFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.describe_your_group , R.string.info_group_description, R.string.hint_description, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { groupDescription ->
            presenter.setGroupDescription(groupDescription)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }
    */

    private fun addGroupMembersFragment() {
        launchContactSelectionFragment()
    }

    // Adapt separate code or use GroupDetailsActivity's displayInviteDialog?
    private fun launchContactSelectionFragment() {
        /* val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_PICK)
        df.setAddMemberDialogListener(object : AddMemberDialog.AddMemberDialogListener {
            override fun contactBook() {
                rxPermissions.request(Manifest.permission.READ_CONTACTS).subscribe({ result ->
                    if (result!!) {
                        // PickContactActivity.startForResult(this@GroupDetailsActivity, GroupDetailsActivity.REQUEST_PICK_CONTACTS)
                    }
                }, { it.printStackTrace() })
            }

            override fun manual() {
                showFillDialog()
            }
        })
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)*/
    }

    /*
    private fun showFillDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_INSERT_MANUAL)
        df.setContactListener { name, phone -> presenter.inviteContact(name, phone) }
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)
    }*/

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun addActionFragment(fragment: MultiOptionPickFragment, group: Group?) {
        disposables.add(fragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.dictate -> {
                    RecordAudioActivity.start(this, REQUEST_DICTATE)
                }
                R.id.takeVote -> {
                    launchVoteSequence(group)
                }
                R.id.createTodo -> {
                    launchTodoSequence(group)
                }
                R.id.createGroup -> {
                    launchGroupSequence()
                }
                R.id.callMeeting -> {
                    launchMeetingSequence(group)
                }
                R.id.createLivewireAlert -> {
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
        // TODO: create group from ground up and register with server
        removeAllViewsAboveCurrent()
        presenter.initTask(CreateActionPresenter.ActionType.Group)

        addGroupNameDescriptionFragment()

        //addGroupMembersFragment()

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
        addTaskDateFragment(GrassrootEntityType.TODO)
        nextStep()

        // or
        // CreateTodoActivity.start(activity)
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
        addVoteOptionsFragment()
        addTaskDateFragment(GrassrootEntityType.VOTE)
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
        //addMeetingSubjectFragment()
        addMeetingLocationFragment()
        addTaskDateFragment(GrassrootEntityType.MEETING)
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

    /*
    private void createGroup() {
        save.setEnabled(false);
        cacheWipGroup();
        progressDialog.show();
        progressDialog.setCancelable(false);

        final Observable<String> sendGroup =  GroupService.getInstance()
                .sendNewGroupToServer(groupUid, AndroidSchedulers.mainThread());

        final Consumer<String> successConsumer = new Consumer<String>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                Log.d(TAG, "string received : " + s);
                progressDialog.dismiss();
                Group finalGroup;
                if (NetworkUtils.SAVED_OFFLINE_MODE.equals(s)) {
                    finalGroup = RealmUtils.loadGroupFromDB(groupUid);
                    handleGroupCreationAndExit(finalGroup, false);
                } else {
                    final String serverUid = s.substring("OK-".length());
                    finalGroup = RealmUtils.loadGroupFromDB(serverUid);
                    Log.d(TAG, "here is the saved group = " + finalGroup.toString());
                    if ("OK".equals(s.substring(0, 2))) {
                        handleGroupCreationAndExit(finalGroup, false);
                    } else {
                        handleSavedButSomeInvalid(serverUid);
                    }
                }
            }
        };*/

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
        } else {
            ItemCreatedFragment.get(presenter.alert!!.groupUid, type)
        }
        adapter.addFragment(f, "")
        nextStep()
    }

    companion object {

        private val EXTRA_GROUP_UID = "groupUid"
        private val EXTRA_FROM_HOME = "from_home"
        private val EXTRA_START_ON_ACTION = "start_on_action"

        private val REQUEST_TAKE_PHOTO = 1
        private val REQUEST_RECORD_VIDEO = 2
        private val REQUEST_GALLERY = 3
        private val REQUEST_RECORD_AUDIO = 4
        private val REQUEST_DICTATE = 5

        fun start(c: Context, groupUid: String?) {
            val i = Intent(c, CreateActionActivity::class.java)
            i.putExtra(EXTRA_GROUP_UID, groupUid)
            c.startActivity(i)
        }

        fun startFromHome(c: Context) {
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
