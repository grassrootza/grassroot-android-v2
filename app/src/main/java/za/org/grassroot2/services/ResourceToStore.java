package za.org.grassroot2.services;

import android.support.annotation.WorkerThread;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public abstract class ResourceToStore<L, R> {

    public ResourceToStore(L localObject, ObservableEmitter<Resource<L>> emitter) {
        uploadRemote(localObject).map(Resource::loading).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(r -> {
            if (r.data.isSuccessful()) {
                saveResult(r.data.body());
                emitter.onNext(Resource.success((L) r.data.body()));
            } else {
                emitter.onNext(Resource.serverError(r.data.errorBody().string(), null));
            }
        }, throwable -> {
            uploadFailed(localObject);
            emitter.onNext(Resource.error(throwable.getMessage(), localObject));
        });
    }

    @WorkerThread
    public abstract Observable<Response<R>> uploadRemote(L localObject);

    @WorkerThread
    public abstract void uploadFailed(L localObject);

    @WorkerThread
    public abstract void saveResult(R data);

}
