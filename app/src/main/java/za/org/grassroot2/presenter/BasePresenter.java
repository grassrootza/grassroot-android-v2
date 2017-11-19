package za.org.grassroot2.presenter;

import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Response;
import timber.log.Timber;
import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.ServerUnreachableException;
import za.org.grassroot2.view.GrassrootView;


public class BasePresenter<T extends GrassrootView> implements GrassrootPresenter {

    protected T view;
    private CompositeDisposable disposables = new CompositeDisposable();


    public void attach(T view) {
        this.view = view;
    }

    public void detach() {
        disposables.clear();
        this.view = null;
    }

    protected void disposableOnDetach(Disposable d) {
        if (!disposables.isDisposed()) {
            disposables.add(d);
        }
    }


    @Override
    public void handleResponseError(Response response) {
    }

    @Override
    public void handleNetworkConnectionError(Throwable t) {
        Timber.d(t);
        view.closeProgressBar();
        view.handleNoConnection();
    }

    @Override
    public void handleNetworkUploadError(Throwable t) {
        Timber.d(t);
        view.closeProgressBar();
        view.handleNoConnectionUpload();
    }

    public void handleAuthenticationError(AuthenticationInvalidException t) {
        Timber.d(t);
        view.closeProgressBar();
    }

    public void handleServerUnreachableError(ServerUnreachableException e) {
        Timber.d(e);
        view.closeProgressBar();
        view.closeKeyboard();
    }

    // these two should only be called internally, to enforce design
    void handleGenericKnownException(Throwable t) {
        Log.e("ERROR", t.toString());
    }


}
