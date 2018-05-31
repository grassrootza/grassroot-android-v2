package za.org.grassroot2.services;

import android.support.annotation.WorkerThread;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public abstract class ResourceToStore<L, R> {

    public ResourceToStore(L localObject, ObservableEmitter<Resource<L>> emitter) {
        uploadRemote(localObject).map(Resource.Companion::loading).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(r -> {
            if (r.getData() != null && r.getData().isSuccessful()) {
                saveResult(r.getData().body());
                Timber.e("inside a resource, finished saving result: %s", r.getData());
                emitter.onNext(Resource.Companion.success((L) r.getData().body()));
                Timber.e("completed saving the result, emitted successfully");
            } else {
                Timber.e("no, that didn't work, server error in resource");
                emitter.onNext(Resource.Companion.serverError(r.getData().errorBody().string(), null));
            }
        }, throwable -> {
            Timber.e("error in resource to store, throws: ", throwable);
            uploadFailed(localObject);
            emitter.onNext(Resource.Companion.error(throwable.getMessage(), localObject));
        });
    }

    @WorkerThread
    public abstract Observable<Response<R>> uploadRemote(L localObject);

    @WorkerThread
    public abstract void uploadFailed(L localObject);

    @WorkerThread
    public abstract void saveResult(R data);

}
