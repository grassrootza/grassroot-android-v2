package za.org.grassroot2.presenter.fragment

import org.greenrobot.eventbus.EventBus

import io.reactivex.Observable
import timber.log.Timber
import za.org.grassroot2.view.FragmentView
import za.org.grassroot2.view.event.MoveNextWithInputEvent


class SingleTextMultiButtonPresenter : BaseFragmentPresenter<SingleTextMultiButtonPresenter.SingleTextMultiButtonView>() {

    override fun onViewCreated() {
        disposableOnDetach(view.inputTextDone().subscribe(
                { charSequence -> EventBus.getDefault().post(MoveNextWithInputEvent(charSequence.toString())) }, { Timber.e(it) }))
    }

    interface SingleTextMultiButtonView : FragmentView {
        fun inputTextDone(): Observable<CharSequence>
        fun setupButtons()
    }
}
