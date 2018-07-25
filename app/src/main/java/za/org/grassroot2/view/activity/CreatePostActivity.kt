package za.org.grassroot2.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_create_action.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.activity.CreatePostPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.MediaPickerFragment
import za.org.grassroot2.view.fragment.ActionSingleInputFragment
import za.org.grassroot2.view.fragment.BackNavigationListener
import za.org.grassroot2.view.fragment.ItemCreatedFragment
import za.org.grassroot2.view.fragment.PostConfirmFragment
import javax.inject.Inject

class CreatePostActivity : GrassrootActivity(), BackNavigationListener, CreatePostPresenter.CreatePostView {

    @Inject lateinit var presenter: CreatePostPresenter
    @Inject lateinit var rxPermission: RxPermissions

    private lateinit var adapter: GenericViewPagerAdapter
    private var created: Boolean = false
    private var shouldRemoveLast: Boolean = false
    private lateinit var groupUid: String
    private lateinit var taskUid: String

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
        taskUid = intent.getStringExtra(EXTRA_TASK_UID)
        groupUid = intent.getStringExtra(EXTRA_GROUP_UID)
        viewPager.adapter = adapter
        presenter.attach(this)
        addSubjectFragment()
        addMediaFragment()
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

    private fun addSubjectFragment() {
        val actionSingleInputFragment = ActionSingleInputFragment.newInstance(R.string.what_is_it_about, R.string.info_post_subject, R.string.hint_post_subject, false)
        disposables.add(actionSingleInputFragment.inputAdded().subscribe { subject ->
            presenter.setSubject(subject)
            nextStep()
        })
        adapter.addFragment(actionSingleInputFragment, "")
    }

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> =
            rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun removeAllViewsAboveCurrent() {
        val current = viewPager!!.currentItem
        adapter.removeAllAbove(current)
    }

    private fun addMediaFragment() {
        val mediaPickerFragment = MediaPickerFragment.get(false)
        disposables.add(mediaPickerFragment.clickAction().subscribe { integer ->
            when (integer) {
                R.id.photo -> presenter.takePhoto()
                R.id.video -> {}
                R.id.gallery -> presenter.pickFromGallery()
            }
        })
        adapter.addFragment(mediaPickerFragment, "")
    }

    private fun displayConfirmFragment() {
        removeAllViewsAboveCurrent()
        val confirmFragment = PostConfirmFragment.newInstance(presenter.postSubject, presenter.currentMediaFileUid != null)
        disposables.add(confirmFragment.taskConfirmed().subscribe({ _ ->
            presenter.createPost(taskUid)
        }, { t -> t.printStackTrace()}))
        adapter.addFragment(confirmFragment, "")
        nextStep()
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

    override fun uploadSuccessfull() {
        created = true
        addSuccessFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                presenter.cameraResult()
            } else {
                data?.let {presenter.handlePickResult(it.data)}
            }
            displayConfirmFragment()
        }
    }

    override fun closeScreen() {
        finish()
    }

    private fun addSuccessFragment() {
        val f: ItemCreatedFragment = ItemCreatedFragment[groupUid, GrassrootEntityType.POST]
        adapter.addFragment(f, "")
        nextStep()
    }

    companion object {

        private val REQUEST_TAKE_PHOTO = 1
        private val REQUEST_RECORD_VIDEO = 2
        private val REQUEST_GALLERY = 3
        private val EXTRA_TASK_UID = "task_uid"
        private val EXTRA_GROUP_UID = "group_uid"

        fun start(c: Context, taskUid: String, groupUid: String) {
            val i = Intent(c, CreatePostActivity::class.java)
            i.putExtra(EXTRA_TASK_UID, taskUid)
            i.putExtra(EXTRA_GROUP_UID, groupUid)
            c.startActivity(i)
        }
    }

}
