package za.org.grassroot2.services

import android.support.annotation.WorkerThread
import io.reactivex.FlowableEmitter
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

abstract class NetworkResource<L, R>(emitter: FlowableEmitter<Resource<L>>) {

    protected var localResultPresent = false

    @WorkerThread
    abstract fun local() : Maybe<L>

    @WorkerThread
    abstract fun remote(): Observable<R>

    init {
        val disposable = local().map { Resource.loading(it) }.subscribe( {
            emitter.onNext(it)
        }, { it.printStackTrace() })
        if (shouldFetch()) {
            remote().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe({ r ->
                disposable.dispose()
                saveResult(r)
                local().map { Resource.success(it) }.subscribe( {
                    emitter.onNext(it)
                    emitter.onComplete()
                })
            }, { it.printStackTrace() })
        }
    }

    @WorkerThread
    abstract fun saveResult(data: R)

    protected abstract fun shouldFetch(): Boolean
}
