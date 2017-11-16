package za.org.grassroot2.presenter

import android.util.Log

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber
import za.org.grassroot2.model.exception.AuthenticationInvalidException
import za.org.grassroot2.model.exception.ServerUnreachableException
import za.org.grassroot2.view.GrassrootView


open class BasePresenter<T : GrassrootView> : GrassrootPresenter {

    protected lateinit var view: T
    private val disposables = CompositeDisposable()


    fun attach(view: T) {
        this.view = view
    }

    fun detach() {
        disposables.clear()
    }

    protected fun disposableOnDetach(d: Disposable) {
        if (!disposables.isDisposed) {
            disposables.add(d)
        }
    }


    override fun handleResponseError(response: Response<*>) {}

    override fun handleNetworkConnectionError(t: Throwable) {
        Timber.d(t)
        view.closeProgressBar()
        view.handleNoConnection()
    }

    override fun handleNetworkUploadError(t: Throwable) {
        Timber.d(t)
        view.closeProgressBar()
        view.handleNoConnectionUpload()
    }

    override fun handleAuthenticationError(t: AuthenticationInvalidException) {
        Timber.d(t)
        view.closeProgressBar()
    }

    override fun handleServerUnreachableError(e: ServerUnreachableException) {
        Timber.d(e)
        view.closeProgressBar()
        view.closeKeyboard()
    }

    fun io(): Scheduler = Schedulers.io()

    fun main(): Scheduler = AndroidSchedulers.mainThread()

    // these two should only be called internally, to enforce design
    internal fun handleGenericKnownException(t: Throwable) {
        Log.e("ERROR", t.toString())
    }

}
