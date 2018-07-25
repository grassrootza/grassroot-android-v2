package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import butterknife.OnClick
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_livewire_confirm.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.alert.LiveWireAlert

class LivewireConfirmFragment : GrassrootFragment() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_livewire_confirm

    private val actionSubject = PublishSubject.create<Boolean>()
    private var listener: BackNavigationListener? = null
    private var headline: String? = null

    override fun onInject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener?
    }

    @OnClick(R.id.backNav, R.id.cancel)
    internal fun back() {
        listener!!.backPressedAndRemoveLast()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        headline = arguments!!.getString(EXTRA_HEADLINE)
        val hasFile = arguments!!.getBoolean(EXTRA_HAS_MEDIA)
        val groupName = arguments!!.getString(EXTRA_GROUP_NAME)
        desc.text = if (hasFile) getString(R.string.lwire_confirm_text_media, headline, groupName) else getString(R.string.lwire_confirm_text_no_media, headline, groupName)
        RxView.clicks(next).map { o -> true }.subscribe(actionSubject)
    }

    fun livewireAlertConfirmed(): Observable<Boolean> {
        return actionSubject
    }

    companion object {

        private const val EXTRA_HEADLINE = "date"
        private const val EXTRA_HAS_MEDIA = "has_media"
        private const val EXTRA_GROUP_NAME = "group_name"

        fun newInstance(alert: LiveWireAlert, groupName: String): LivewireConfirmFragment {
            val f = LivewireConfirmFragment()
            val b = Bundle()
            b.putString(EXTRA_HEADLINE, alert.headline)
            b.putBoolean(EXTRA_HAS_MEDIA, alert.mediaFile != null)
            b.putString(EXTRA_GROUP_NAME, groupName)
            f.arguments = b
            return f
        }
    }

}
