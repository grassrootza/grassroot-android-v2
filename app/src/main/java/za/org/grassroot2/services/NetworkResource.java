package za.org.grassroot2.services;

import android.support.annotation.WorkerThread;

import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class NetworkResource<L, R> {

    public NetworkResource(FlowableEmitter<Resource<L>> emitter) {
        Disposable local = getLocal().map(Resource::loading).subscribe(emitter::onNext, Throwable::printStackTrace);
        if (shouldFetch()) {
            getRemote().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(r -> {
                local.dispose();
                saveResult(r);
                getLocal().map(Resource::success).subscribe(emitter::onNext);
            }, Throwable::printStackTrace);
        }
    }

    @WorkerThread
    public abstract Single<L> getLocal();

    @WorkerThread
    public abstract Observable<R> getRemote();

    @WorkerThread
    public abstract void saveResult(R data);

    protected abstract boolean shouldFetch();
}
