package za.org.grassroot2.view.fragment

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_group_about.*
import kotlinx.android.synthetic.main.fragment_me.*
import timber.log.Timber
import za.org.grassroot2.BuildConfig
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.fragment.GroupAboutPresenter
import javax.inject.Inject

/**
 * Created by luke on 2017/12/04.
 */
class GroupAboutFragment : GrassrootFragment(), GroupAboutPresenter.GroupAboutView {

    @Inject lateinit var presenter: GroupAboutPresenter

    override fun getLayoutResourceId(): Int = R.layout.fragment_group_about

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter.attach(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.init(arguments.getString(EXTRA_GROUP_UID)!!)
        presenter.loadGroup()
    }

    override fun render(group: Group) {
        Timber.e("rendering group ... setting description to: " + group.description)
        description.text = group.description
        loadProfilePic(group.uid)
        // todo : show and hide some buttons depending on permissions?
    }

    private fun loadProfilePic(groupUid: String) {
        val url = BuildConfig.API_BASE + "group/image/view/" + groupUid
        Picasso.with(context)
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

    companion object {
        private val EXTRA_GROUP_UID = "group_uid"

        fun newInstance(groupUid: String): Fragment {
            val fragment = GroupAboutFragment()
            val b = Bundle()
            b.putString(EXTRA_GROUP_UID, groupUid)
            fragment.arguments = b
            return fragment
        }
    }

}