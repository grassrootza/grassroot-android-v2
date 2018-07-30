package za.org.grassroot2.presenter.fragment

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.FragmentView

/**
 * Created by qbasso on 21.09.2017.
 */

class MeetingDatePresenter @Inject
internal constructor(private val networkService: NetworkService) : BaseFragmentPresenter<MeetingDatePresenter.MeetingDateView>() {

    override fun onViewCreated() {
        disposableOnDetach(view.dateInputConfirmed().observeOn(AndroidSchedulers.mainThread())
                .flatMap { s -> networkService.getTimestampForText(s) }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ timestamp -> view.renderDate(timestamp) }, { throwable ->
                    throwable.printStackTrace()
                    view.showDatePicker()
                }))
    }

    interface MeetingDateView : FragmentView {
        fun dateInputConfirmed(): Observable<String>
        fun renderDate(timestamp: Long?)
        fun showDatePicker()
    }
}
