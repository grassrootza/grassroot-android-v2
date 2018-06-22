package za.org.grassroot2.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_me.*
import timber.log.Timber
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.presenter.MePresenter
import za.org.grassroot2.view.MeView
import za.org.grassroot2.view.activity.WelcomeActivity
import javax.inject.Inject


class MeFragment : GrassrootFragment(), MeView {

    @Inject lateinit var presenter: MePresenter
    @Inject lateinit var rxPermission: RxPermissions

    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_GALLERY = 2

    private lateinit var languagesAdapter: ArrayAdapter<Language>

    private val dataChangeWatcher = ProfileDataChangeWatcher()

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

        profilePhoto.setOnClickListener {
            showPopup(profilePhoto)
        }

        changePhoto.setOnClickListener {
            showPopup(changePhoto)
        }

        initToolbar()
        displayNameInput.addTextChangedListener(dataChangeWatcher)
        phoneNumberInput.addTextChangedListener(dataChangeWatcher)
        emailInput.addTextChangedListener(dataChangeWatcher)
        languageInput.onItemSelectedListener = dataChangeWatcher

        saveBtn.setOnClickListener {
            val language = languageInput.selectedItem as Language?
            val languageCode = language?.code ?: "en"
            presenter.updateProfileData(
                    displayNameInput.text.toString(),
                    phoneNumberInput.text.toString(),
                    emailInput.text.toString(),
                    languageCode
            )
        }

        presenter.attach(this)
        presenter.onViewCreated()
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.title_me)
    }

    override fun onResume() {
        super.onResume()
        Timber.e("resuming fragment, invalidating options menu")
        (activity as AppCompatActivity).invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.e("calling on create options menu inside me fragment")
        inflater?.inflate(R.menu.fragment_me, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_logout -> presenter.logout()
        }
        return super.onOptionsItemSelected(item)
    }


    fun showPopup(v: View) {
        val popup = PopupMenu(activity!!, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.change_image_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.fromGallery -> presenter.pickFromGallery()
                R.id.useCamera -> presenter.takePhoto()
            }
            true
        }
        popup.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("Showing solitary progress bar in MeFragment")
        showProgressBar()
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
        emailInput.setText(profile.emailAddress)

        val languageObservable = presenter.getLanguages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { languageMap ->
                            val langaugeList = languageMap.toList().map { Language(it.first, it.second) }
                            var selectedPosition = 0
                            for ((index, lang) in langaugeList.withIndex()) {
                                if (lang.code == profile.languageCode)
                                    selectedPosition = index
                            }
                            languagesAdapter = ArrayAdapter(activity, R.layout.item_language, langaugeList)
                            languageInput.adapter = languagesAdapter
                            languageInput.setSelection(selectedPosition)
                        },
                        {
                            Timber.e(it)
                        }
                )

        presenter.addDisposableOnDetach(languageObservable)

        loadProfilePic(profile.uid)
        submitActions.visibility = View.INVISIBLE
    }

    override fun invalidateProfilePicCache(userUid: String) {
        val url = BuildConfig.API_BASE + "user/profile/image/view/" + userUid
        Picasso.get().invalidate(url)
        loadProfilePic(userUid)
    }

    private fun loadProfilePic(userUid: String) {
        val url = BuildConfig.API_BASE + "user/profile/image/view/" + userUid
        Picasso.get()
                .load(url)
                .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .centerCrop()
                .into(profilePhoto, object : Callback {
                    override fun onSuccess() {
                        val imageBitmap = (profilePhoto.drawable as BitmapDrawable).bitmap
                        val imageDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, imageBitmap)
                        imageDrawable.isCircular = true
                        imageDrawable.cornerRadius = Math.max(imageBitmap.width, imageBitmap.height) / 2.0f
                        profilePhoto.setImageDrawable(imageDrawable)
                    }

                    override fun onError(e: Exception?) {
                        profilePhoto?.setImageResource(R.drawable.user)
                    }
                })

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


    inner class ProfileDataChangeWatcher : TextWatcher, OnItemSelectedListener {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            submitActions.visibility = View.VISIBLE
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
            val selectedLanguage = languagesAdapter.getItem(position)
            if (selectedLanguage == null || !presenter.isCurrentLanguage(selectedLanguage.code))
                submitActions.visibility = View.VISIBLE
        }
    }

    class Language(val code: String, val name: String) {
        override fun toString(): String {
            return name
        }
    }

    override fun returnToWelcomeActivity() {
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}