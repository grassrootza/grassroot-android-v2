package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.Menu
import android.view.View
import com.github.florent37.viewtooltip.ViewTooltip
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_group_details.*
import za.org.grassroot2.GroupSettingsActivity
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.extensions.getColorCompat
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.activity.GroupDetailsPresenter
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.AddMemberDialog
import za.org.grassroot2.view.dialog.SelectImageDialog
import za.org.grassroot2.view.fragment.GroupTasksFragment
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class GroupDetailsActivity : GrassrootActivity(), GroupDetailsPresenter.GroupDetailsView,SelectImageDialog.SelectImageDialogEvents {


    private var groupUid: String? = null

    @Inject lateinit var presenter: GroupDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions
    @Inject lateinit var groupFragmentPresenter:GroupFragmentPresenter
    @Inject lateinit var rxPermission: RxPermissions

    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_GALLERY = 2
    private var groupImageUrl:String = ""

    private val DIALOG_CAMERA = "Camera"
    private val DIALOG_GALLERY = "Gallery"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupUid = intent.getStringExtra(EXTRA_GROUP_UID)
        initView()
        presenter.attach(this)
        presenter.init(groupUid!!)
        fab.setOnClickListener { CreateActionActivity.start(activity, groupUid) }
        inviteMembers.setOnClickListener { displayInviteDialog() }
        about.setOnClickListener { launchGroupSettings() }

        addPhoto.setOnClickListener(View.OnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),3)
            }else{
                //uploadImage()
                SelectImageDialog.newInstance(R.string.select_image,false).show(supportFragmentManager, DIALOG_TAG)
            }
        })
    }

    override fun openCamera() {
        presenter.takePhoto()
    }

    override fun pickImageFromGallery() {
        presenter.pickFromGallery()
    }

    private fun uploadImage(){
        val items = arrayOf<CharSequence>(DIALOG_CAMERA,DIALOG_GALLERY)
        val alertDialog:AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("Upload image")

        alertDialog.setItems(items) { _, which ->
            val selected = items[which]
            if(selected.equals(DIALOG_CAMERA)){
                presenter.takePhoto()
            } else if(selected.equals(DIALOG_GALLERY)){
                presenter.pickFromGallery()
            }
        }

        val dialog = alertDialog.create()
        dialog.show()
    }

    override fun cameraForResult(contentProviderPath: String, s: String) {
        val intent:Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath))
        intent.putExtra("MY_UID", s)
        startActivityForResult(intent,REQUEST_TAKE_PHOTO)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_group_details
    }

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
                        PickContactActivity.startForResult(this@GroupDetailsActivity, REQUEST_PICK_CONTACTS)
                    }
                }, { it.printStackTrace() })
            }

            override fun manual() {
                showFillDialog()
            }
        })
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)
    }

    private fun launchGroupSettings() {
        GroupSettingsActivity.start(this, groupUid)
    }

    private fun showFillDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_INSERT_MANUAL)
        df.setContactListener { name, phone -> presenter.inviteContact(name, phone) }
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)
    }

    private fun initView() {
        initTabs()
        initToolbar()
    }

    private fun initTabs() {
        val adapter = GenericViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, null), getString(R.string.title_all))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.VOTE), getString(R.string.title_votes))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.MEETING), getString(R.string.title_meetings))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.TODO), getString(R.string.title_todos))
        contentPager.adapter = adapter
        tabs!!.setupWithViewPager(contentPager)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v ->
            run {
                groupFragmentPresenter.refreshGroups()
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO){
                presenter.cameraResult()

            } else if(requestCode == REQUEST_GALLERY){
                val imageUri:Uri = data!!.data

                presenter.setGroupImageUrl(imageUri.toString())
                setImage(imageUri.toString())
            } else if (requestCode == REQUEST_PICK_CONTACTS && resultCode == Activity.RESULT_OK) {
                val contacts = data!!.getSerializableExtra(PickContactActivity.EXTRA_CONTACTS) as? ArrayList<Contact>
                presenter.inviteContacts(contacts)
            }
        }
    }

    override fun setImage(imageUrl:String){
        val imageUri:Uri = Uri.parse(imageUrl)
        Picasso.get()
                .load(imageUri)
                .into(image, object: Callback {
                    override fun onSuccess() {
                        val imageBitmap = (image.drawable as BitmapDrawable).bitmap
                        image.setImageBitmap(imageBitmap)
                    }

                    override fun onError(e: Exception?) {
                    }

                })
    }

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun render(group: Group) {
        groupTitle.text = group.name

        if(group.profileImageUrl != null){
            groupImageUrl = group.profileImageUrl
            setImage(groupImageUrl)
        }
    }

    override fun emptyData() {
        ViewTooltip.on(fab)
                .color(getColorCompat(R.color.light_green))
                .corner(10).autoHide(true, 2000).padding(10, 10, 10, 10).align(ViewTooltip.ALIGN.CENTER)
                .position(ViewTooltip.Position.LEFT).text(R.string.info_group_create_item).setTextGravity(Gravity.CENTER)
                .show()
    }

    override fun displayFab() {
        fab!!.visibility = View.VISIBLE
    }

    companion object {

        private val EXTRA_GROUP_UID = "group_uid"
        private val REQUEST_PICK_CONTACTS = 1

        fun start(activity: Activity, groupUid: String) {
            val intent = Intent(activity, GroupDetailsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_UID, groupUid)
            activity.startActivity(intent)
        }
    }
}
