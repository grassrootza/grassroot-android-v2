package za.org.grassroot2.presenter.fragment

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.view.FragmentView


abstract class BaseFragmentPresenter<T : FragmentView> {

    protected lateinit var view: T
    private val disposables = CompositeDisposable()

    abstract fun onViewCreated()

    open fun attach(view: T) {
        this.view = view
        EventBus.getDefault().register(this)
    }

    open fun detach(view: T) {
        disposables.clear()
        EventBus.getDefault().unregister(this)
    }

    protected fun disposableOnDetach(d: Disposable) {
        if (!disposables.isDisposed) {
            disposables.add(d)
        }
    }

    @Subscribe
    fun emptyEvent(o: Any) {
    }

    fun handleNetworkConnectionError(t: Throwable) {
        Timber.d(t)
        view.handleNoConnection()
    }

    fun handleNetworkUploadError(t: Throwable) {
        Timber.d(t)
        view.handleNoConnectionUpload()
    }

    fun io(): Scheduler = Schedulers.io()

    fun main(): Scheduler = AndroidSchedulers.mainThread()

}
