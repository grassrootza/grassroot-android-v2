package za.org.grassroot2.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_me.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.presenter.MePresenter
import za.org.grassroot2.view.MeView
import javax.inject.Inject

class MeFragment : GrassrootFragment(), MeView {

    @Inject lateinit var presenter: MePresenter
    @Inject lateinit var rxPermission: RxPermissions

    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_GALLERY = 2


    override fun onInject(component: ActivityComponent) {
        get().inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_me
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.title_me)
        profilePhoto.setOnClickListener {
            presenter.pickFromGallery()
        }
        presenter.attach(this)
        presenter.onViewCreated()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                presenter.cameraResult()
            } else {
                presenter.handlePickResult(data!!.data)
            }
        }
    }


    override fun ensureWriteExteralStoragePermission(): Observable<Boolean> {
        return rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun displayUserData(profile: UserProfile) {
        displayNameInput.setText(profile.displayName)
        phoneNumberInput.setText(profile.msisdn)
//        emailInput.setText(profile.email)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach(this)
    }


    override fun cameraForResult(contentProviderPath: String, s: String) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(contentProviderPath))
        cameraIntent.putExtra("MY_UID", s)
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
    }


    override fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    companion object {
        fun newInstance(): Fragment {
            return MeFragment()
        }
    }
}