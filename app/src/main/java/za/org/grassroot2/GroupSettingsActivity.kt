package za.org.grassroot2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.Toast
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_group_settings.*
import timber.log.Timber
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.activity.GroupSettingsPresenter
import za.org.grassroot2.view.activity.DashboardActivity
import za.org.grassroot2.view.activity.GrassrootActivity
import javax.inject.Inject

class GroupSettingsActivity : GrassrootActivity(), GroupSettingsPresenter.GroupSettingsView {

    @Inject lateinit var presenter: GroupSettingsPresenter
    @Inject lateinit var rxPermission: RxPermissions

    private var groupUid: String? = null

    override fun getLayoutResourceId(): Int = R.layout.activity_group_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupUid = intent.getStringExtra(EXTRA_GROUP_UID)
        presenter.attach(this)
        presenter.init(groupUid!!)
        initToolbar()
        hideGroup.setOnClickListener { presenter.hideGroup() }
        leaveGroup.setOnClickListener { presenter.leaveGroup() }
        exportGroup.setOnClickListener{ presenter.exportMembers() }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun render(group: Group) {
        Timber.e("rendering group ... setting description to: " + group.description)
        supportActionBar!!.setTitle(group.name)
        description.text = group.description
        loadProfilePic(group.uid)
    }

    private fun loadProfilePic(groupUid: String) {
        val url = BuildConfig.API_BASE + "group/image/view/" + groupUid
        Picasso.with(this)
                .load(url)
                .resizeDimen(R.dimen.profile_photo_s_width, R.dimen.profile_photo_s_height)
                .placeholder(R.drawable.group_5)
                .error(R.drawable.group_5)
                .centerCrop()
                .into(groupPhoto, object : Callback {
                    override fun onSuccess() {
                        val imageBitmap = (groupPhoto.drawable as BitmapDrawable).bitmap
                        val imageDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                        imageDrawable.isCircular = true
                        imageDrawable.cornerRadius = Math.max(imageBitmap.width, imageBitmap.height) / 2.0f
                        groupPhoto.setImageDrawable(imageDrawable)
                    }

                    override fun onError() {
                        groupPhoto.setImageResource(R.drawable.user)
                    }
                })
    }


    override fun exitToHome(messageToUser: Int) {
        Toast.makeText(this, messageToUser, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    companion object {
        private val EXTRA_GROUP_UID = "group_uid";

        fun start(activity: Activity, groupUid: String?) {
            val intent = Intent(activity, GroupSettingsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_UID, groupUid)
            activity.startActivity(intent)
        }
    }
}
