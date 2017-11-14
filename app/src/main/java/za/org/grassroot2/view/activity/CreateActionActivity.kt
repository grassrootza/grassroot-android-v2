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
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.GroupPermission
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.CreateActionPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.MediaPickerFragment
import za.org.grassroot2.view.dialog.MultiOptionPickFragment
import za.org.grassroot2.view.fragment.*
import javax.inject.Inject

class CreateActionActivity : GrassrootActivity(), BackNavigationListener, CreateActionPresenter.CreateActionView {

    @Inject lateinit var presenter: CreateActionPresenter
    @Inject lateinit var rxPermission: RxPermissions

    private lateinit var adapter: GenericViewPagerAdapter
    private var created: Boolean = false
    private var shouldRemoveLast: Boolean = false

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_create_action
    }

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
        val createActionFragment = MultiOptionPickFragment.getHomeActionPicker()
        disposables.add(createActionFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.createGroup -> {
                }
                R.id.takeAction -> {
                    addActionFragment(MultiOptionPickFragment.homeTakeActionFragment(), null)
                    nextStep()
                }
                R.id.dictate -> {
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

    private fun addMeetingDateFragment() {
        val meetingDateFragment = MeetingDateFragment()
        disposables.add(meetingDateFragment.meetingDatePicked().subscribe { date ->
            closeKeyboard()
            presenter.setMeetingDate(date)
            val f = MeetingDateConfirmFragment.newInstance(date!!)
            disposables.add(f.meetingDateConfirmed().subscribe({ _ -> presenter.createMeeting() }, { it.printStackTrace() }))
            adapter.addFragment(f, "")
            nextStep()
            shouldRemoveLast = true
        })
        adapter.addFragment(meetingDateFragment, "")
    }

    private fun addMeetingLocationFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.where_will_it_happen, R.string.info_meeting_location, R.string.hint_meeting_location, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { location ->
            presenter.setMeetingLocation(location)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    private fun addMeetingSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_meeting_subject, R.string.hint_meeting_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setSubject(subject)
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
            val f = LivewireConfirmFragment.newInstance(entry.value, entry.key)
            adapter.addFragment(f, "")
            disposables.add(f.livewireAlertConfirmed().subscribe({ _ -> presenter.createAlert() }, { it.printStackTrace() }))
            nextStep()
            shouldRemoveLast = true
        }, { it.printStackTrace() }))
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

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun addActionFragment(fragment: MultiOptionPickFragment, group: Group?) {
        disposables.add(fragment.clickAction().subscribe { integer ->
            when (integer) {
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
        addMeetingDateFragment()
        nextStep()
    }

    private fun removeAllViewsAboveCurrent() {
        val current = viewPager!!.currentItem
        adapter.removeAllAbove(current)
    }

    private fun addMediaFragment() {
        val mediaPickerFragment = MediaPickerFragment.getMediaPicker()
        disposables.add(mediaPickerFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.photo -> presenter.takePhoto()
                R.id.video -> presenter.recordVideo()
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
            if (requestCode == REQUEST_TAKE_PHOTO) {
                presenter.cameraResult()
            } else {
                presenter.handlePickResult(data.data)
            }
            nextStep()
        }
    }

    override fun closeScreen() {
        finish()
    }

    private fun addSuccessFragment(type: GrassrootEntityType) {
        val f: ItemCreatedFragment = if (type == GrassrootEntityType.MEETING) {
            ItemCreatedFragment.get(presenter.task.parentUid, type)
        } else {
            ItemCreatedFragment.get(presenter.alert.groupUid, type)
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
        }

    }

}
