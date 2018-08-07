package za.org.grassroot2.services

import android.support.annotation.WorkerThread

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber

abstract class ResourceToStore<L, R>(localObject: L, emitter: ObservableEmitter<Resource<L>>) {

    init {
        uploadRemote(localObject).map<Resource<Response<R>>> { it -> Resource.loading(it) }
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({ r ->
                    if (r.data != null && r.data.isSuccessful) {
                        saveResult(r.data.body())
                        Timber.e("inside a resource, finished saving result: %s", r.data)
                        emitter.onNext(Resource.success(r.data.body() as L))
                        Timber.e("completed saving the result, emitted successfully")
                    } else {
                        Timber.e("no, that didn't work, server error in resource")
                        emitter.onNext(Resource.serverError(r.data!!.errorBody()!!.string(), null))
                    }
                }) { throwable ->
                    Timber.e(throwable, "error in resource to store, throws: ")
                    uploadFailed(localObject)
                    emitter.onNext(Resource.error(throwable.message!!, localObject))
                }
    }

    @WorkerThread
    abstract fun uploadRemote(localObject: L): Observable<Response<R>>

    @WorkerThread
    abstract fun uploadFailed(localObject: L)

    @WorkerThread
    abstract fun saveResult(data: R?)

}
