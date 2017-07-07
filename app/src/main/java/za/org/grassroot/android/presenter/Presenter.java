package za.org.grassroot.android.presenter;

import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import za.org.grassroot.android.view.GrassrootView;

/**
 * Created by luke on 2017/07/06.
 */

abstract class Presenter {

    GrassrootView view;
    CompositeDisposable subscriptions;

    public abstract void attach(GrassrootView view);
    public abstract void detach(GrassrootView view);

    protected abstract void onViewAttached();
    protected abstract void onViewDetached();

    void handleException(Throwable t) {
        Log.e("ERROR", t.toString());
        // add toast & exit to main
    }

    void handleNetworkConnectionError(Throwable t) {
        Log.e("CONNECTION", t.toString());
        view.showConnectionFailedDialog().subscribe();
    }

    void handleAuthenticationError(Throwable t) {
        Log.e("AUTHENTICATION", t.toString());
        view.showAuthenticationRecoveryDialog().subscribe();
    }

}
