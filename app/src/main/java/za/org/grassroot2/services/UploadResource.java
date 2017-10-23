package za.org.grassroot2.services;

import android.support.annotation.WorkerThread;

import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public abstract class UploadResource<L> {

    public UploadResource(L localObject, ObservableEmitter<Response<Void>> emitter) {
        uploadRemote(localObject).subscribe(emitter::onNext, throwable -> {
            uploadFailed(localObject);
            emitter.onError(throwable);
        });
    }

    @WorkerThread
    public abstract Observable<Response<Void>> uploadRemote(L localObject);

    @WorkerThread
    public abstract void uploadFailed(L localObject);

}
