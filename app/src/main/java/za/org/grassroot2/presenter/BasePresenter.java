package za.org.grassroot2.presenter;

import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.NetworkUnavailableException;
import za.org.grassroot2.model.exception.ServerUnreachableException;
import za.org.grassroot2.view.GrassrootView;


public abstract class BasePresenter<T extends GrassrootView> implements GrassrootPresenter {

    protected T view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public void attach(T view) {
        this.view = view;
    }

    public void detach(T view) {
        disposables.clear();
        this.view = null;
    }

    protected void disposableOnDetach(Disposable d) {
        if (!disposables.isDisposed()) {
            disposables.add(d);
        }
    }

    @Override
    public void handleNetworkConnectionError(NetworkUnavailableException t) {
        Log.e("CONNECTION", t.toString());
        view.closeProgressBar();
        view.showConnectionFailedDialog().subscribe();
    }

    public void handleAuthenticationError(AuthenticationInvalidException t) {
        Log.e("AUTHENTICATION", t.toString());
        view.closeProgressBar();
        view.showAuthenticationRecoveryDialog().subscribe();
    }

    public void handleServerUnreachableError(ServerUnreachableException e) {
        Log.e("SERVER", e.toString());
        view.closeProgressBar();
        view.closeKeyboard();
//        view.showErrorToast(R.string.error_server_unreachable);
    }

    // these two should only be called internally, to enforce design
    void handleGenericKnownException(Throwable t) {
        Log.e("ERROR", t.toString());
    }

    void handleUnknownError(Exception e) {
        e.printStackTrace();
//        view.showErrorToast(R.string.error_unknown_generic);
    }

}
