package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import butterknife.OnClick
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_meeting_date_confirm.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.util.DateFormatter

class MeetingDateConfirmFragment : GrassrootFragment() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_meeting_date_confirm

    private val actionSubject = PublishSubject.create<Long>()
    private var listener: BackNavigationListener? = null
    private var dateToConfirm: Long = 0

    fun meetingDateConfirmed(): Observable<Long> = actionSubject

    override fun onInject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener?
    }

    // todo: strip this on way to removing Butterknife now that have Kotlin
    @OnClick(R.id.backNav, R.id.cancel)
    internal fun back() {
        listener!!.backPressedAndRemoveLast()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateToConfirm = arguments!!.getLong(EXTRA_DATE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        date.text = DateFormatter.formatDate(dateToConfirm)
        year.text = DateFormatter.formatYear(dateToConfirm)
        time.text = DateFormatter.formatTime(dateToConfirm)
        RxView.clicks(next!!).map { o -> dateToConfirm }.subscribe(actionSubject)
    }


    companion object {

        private const val EXTRA_DATE = "date"

        fun newInstance(date: Long): MeetingDateConfirmFragment {
            val f = MeetingDateConfirmFragment()
            val b = Bundle()
            b.putLong(EXTRA_DATE, date)
            f.arguments = b
            return f
        }
    }

}
