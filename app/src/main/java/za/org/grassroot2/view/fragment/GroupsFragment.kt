package za.org.grassroot2.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_groups.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.view.activity.CreateActionActivity
import za.org.grassroot2.view.activity.GroupDetailsActivity
import za.org.grassroot2.view.adapter.GroupsAdapter
import za.org.grassroot2.view.dialog.SelectImageDialog
import javax.inject.Inject

class GroupsFragment : GrassrootFragment(), GroupFragmentPresenter.GroupFragmentView,SelectImageDialog.SelectImageDialogEvents {
    override val layoutResourceId: Int
        get() = R.layout.fragment_groups

    override fun stopRefreshing() {
        refreshLayout.isRefreshing = false
    }

    @Inject lateinit internal var presenter: GroupFragmentPresenter
    private lateinit var groupsAdapter: GroupsAdapter
    @Inject lateinit var rxPermission: RxPermissions

    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_GALLERY = 2

    private lateinit var selectImageDialog:SelectImageDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_groups, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.title_groups)
        fab.setOnClickListener { CreateActionActivity.startFromHome(activity as AppCompatActivity) }
        refreshLayout.setOnRefreshListener { presenter.refreshGroups() }
        presenter.attach(this)
        presenter.onViewCreated()
    }


    override fun setImage(imageUrl: String?) {
        groupsAdapter.setImage(imageUrl!!,presenter.getUid())
        selectImageDialog.dismiss()
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }

    override fun render(groups: List<Group>) {
        groupRecyclerView.visibility = View.VISIBLE
        emptyInfoContainer.visibility = View.GONE
        groupRecyclerView.layoutManager = LinearLayoutManager(activity)
        groupsAdapter = GroupsAdapter(activity!!, groups)
        val footer = LayoutInflater.from(activity).inflate(R.layout.item_group_footer, null, false)
        groupsAdapter.addFooter(footer)
        groupRecyclerView.adapter = groupsAdapter
    }

    override fun renderEmpty() {
        displayEmptyLayout()
        emptyInfo!!.setText(R.string.no_group_info)
    }

    override fun itemClick(): Observable<String> {
        return groupsAdapter.viewClickObservable
    }

    override fun groupImageClick(): Observable<String> {
       return groupsAdapter.groupImageClickObservable
    }

    override fun openCamera() {
        presenter.takePhoto()
    }

    override fun pickImageFromGallery() {
        presenter.pickFromGallery()
    }

    override fun cameraForResult(contentProviderPath: String, s: String) {
        val intent:Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath))
        intent.putExtra("MY_UID", s)
        startActivityForResult(intent,REQUEST_TAKE_PHOTO)
    }

    override fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity?.startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun renderEmptyFailedSync() {
        displayEmptyLayout()
        emptyInfo!!.setText(R.string.sync_problem)
    }

    override fun openDetails(groupUid: String) {
        activity?.let { GroupDetailsActivity.start(it, groupUid) }
    }

    override fun openSelectImageDialog() {
        selectImageDialog = SelectImageDialog.newInstance(R.string.select_image,true)
        selectImageDialog.setTargetFragment(this,0)
        selectImageDialog.show(activity?.supportFragmentManager,"DIALOG_TAG")
    }

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                presenter.cameraResult()
            } else if (requestCode == REQUEST_GALLERY) {
                val imageUri:Uri = data!!.data

                presenter.setGroupImageUrl(imageUri.toString())
                setImage(imageUri.toString())
            }
        }
    }

    private fun displayEmptyLayout() {
        emptyInfoContainer.visibility = View.VISIBLE
        groupRecyclerView.visibility = View.GONE
    }

    companion object {

        fun newInstance(): GroupsFragment {
            val fragment = GroupsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
