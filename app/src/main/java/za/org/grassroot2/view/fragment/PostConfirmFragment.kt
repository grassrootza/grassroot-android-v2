package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_task_confirm.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent


class PostConfirmFragment : GrassrootFragment() {

    private val actionSubject = PublishSubject.create<Boolean>()
    private var listener: BackNavigationListener? = null
    private var title: String? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = arguments!!.getString(EXTRA_TITLE)
        val hasFile = arguments!!.getBoolean(EXTRA_HAS_MEDIA)
        desc.text = if (hasFile) getString(R.string.post_confirm_text_media, title) else getString(R.string.post_confirm_text_no_media, title)
        RxView.clicks(next).map { _ -> true }.subscribe(actionSubject)
        backNav.setOnClickListener { listener!!.backPressedAndRemoveLast() }
        cancel.setOnClickListener { listener!!.backPressedAndRemoveLast() }
    }

    fun taskConfirmed(): Observable<Boolean> = actionSubject

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.fragment_task_confirm

    companion object {

        private val EXTRA_TITLE = "title"
        private val EXTRA_HAS_MEDIA = "has_media"

        fun newInstance(title: String, hasMedia: Boolean): PostConfirmFragment {
            val f = PostConfirmFragment()
            val b = Bundle()
            b.putString(EXTRA_TITLE, title)
            b.putBoolean(EXTRA_HAS_MEDIA, hasMedia)
            f.arguments = b
            return f
        }
    }

}
