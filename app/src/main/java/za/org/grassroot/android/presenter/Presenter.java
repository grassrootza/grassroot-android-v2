package za.org.grassroot.android.presenter;

import android.support.annotation.CallSuper;
import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.exception.AuthenticationInvalidException;
import za.org.grassroot.android.model.exception.LifecycleOutOfSyncException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.exception.ServerUnreachableException;
import za.org.grassroot.android.view.GrassrootView;

/**
 * Created by luke on 2017/07/06.
 */

public abstract class Presenter {

    private GrassrootView view;
    CompositeDisposable subscriptions;

    @CallSuper
    public void attach(GrassrootView view) {
        this.view = view;
        this.subscriptions = new CompositeDisposable();
    }

    @CallSuper
    public void detach(GrassrootView view) {
        this.view = null;
    }

    protected abstract void onViewAttached();

    @CallSuper
    protected void onViewDetached() {
        try {
            subscriptions.dispose();
        } catch (NullPointerException e) {
            handleGenericKnownException(new LifecycleOutOfSyncException());
        }
    }

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
        view.showErrorToast(R.string.error_server_unreachable);
    }

    // these two should only be called internally, to enforce design
    void handleGenericKnownException(Throwable t) {
        Log.e("ERROR", t.toString());
        // add toast & exit to main
    }

    void handleUnknownError(Exception e) {
        e.printStackTrace();
        view.showErrorToast(R.string.error_unknown_generic);
    }

}