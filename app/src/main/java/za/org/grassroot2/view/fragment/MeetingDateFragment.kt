package za.org.grassroot2.view.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import butterknife.OnClick
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_meeting_date.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.fragment.MeetingDatePresenter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MeetingDateFragment : GrassrootFragment(), MeetingDatePresenter.MeetingDateView {

    @Inject lateinit var presenter: MeetingDatePresenter
    private val seletedDate = Calendar.getInstance()

    private val actionSubject = PublishSubject.create<Long>()
    private var listener: BackNavigationListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener
    }

    @OnClick(R.id.backNav)
    internal fun back() {
        listener!!.backPressed()
    }

    @OnClick(R.id.cancel)
    internal fun close() {
        activity.finish()
    }

    @OnClick(R.id.pickDate)
    internal fun pickDate() {
        val c = Calendar.getInstance()
        val d = DatePickerDialog(activity, { view, year, month, dayOfMonth ->
            seletedDate.set(year, month, dayOfMonth)
            pickTime()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        d.show()
    }

    private fun pickTime() {
        val c = Calendar.getInstance()
        val tpd = TimePickerDialog(activity, { view, hourOfDay, minute ->
            seletedDate.set(Calendar.HOUR, hourOfDay)
            seletedDate.set(Calendar.MINUTE, minute)
            actionSubject.onNext(seletedDate.timeInMillis)
        }, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false)
        tpd.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.onViewCreated()
        disposables.add(RxTextView.textChanges(dateInput!!)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ charSequence -> next!!.isEnabled = charSequence.length > 0 }, { t -> t.printStackTrace() }))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach(this)
    }

    fun meetingDatePicked(): Observable<Long> {
        return actionSubject
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_meeting_date
    }

    override fun dateInputConfirmed(): Observable<String> {
        return Observable.merge(
                RxView.clicks(next!!).map { o -> dateInput.text.toString() },
                RxTextView.editorActionEvents(dateInput)
                        .filter { textViewEditorActionEvent -> textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_DONE }
                        .map { textViewEditorActionEvent -> dateInput.text.toString() }).subscribeOn(AndroidSchedulers.mainThread())
    }

    override fun renderDate(timestamp: Long?) {
        closeProgressBar()
        seletedDate.timeInMillis = timestamp!!
        actionSubject.onNext(seletedDate.timeInMillis)
    }

    override fun showDatePicker() {
        pickDate()
    }
}
