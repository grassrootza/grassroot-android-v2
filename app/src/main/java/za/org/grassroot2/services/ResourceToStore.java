package za.org.grassroot2.services;

import android.support.annotation.WorkerThread;

import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public abstract class ResourceToStore<L, R> {

    public ResourceToStore(L localObject, FlowableEmitter<Resource<L>> emitter) {
        uploadRemote(localObject).map(Resource::loading).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(r -> {
            saveResult(r.data);
            emitter.onNext(Resource.success((L) r.data));
        }, throwable -> {
            emitter.onNext(Resource.error("Upload failed", localObject));
            uploadFailed(localObject);
        });
    }

    @WorkerThread
    public abstract Observable<R> uploadRemote(L localObject);

    @WorkerThread
    public abstract void uploadFailed(L localObject);

    @WorkerThread
    public abstract void saveResult(R data);

}
