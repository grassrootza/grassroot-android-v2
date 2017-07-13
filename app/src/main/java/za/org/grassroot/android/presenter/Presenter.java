package za.org.grassroot.android.presenter;

import android.support.annotation.CallSuper;
import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import za.org.grassroot.android.R;
import za.org.grassroot.android.model.exception.AuthenticationInvalidException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.view.GrassrootView;

/**
 * Created by luke on 2017/07/06.
 */

abstract class Presenter {

    GrassrootView view;
    CompositeDisposable subscriptions;

    @CallSuper
    public void attach(GrassrootView view) {
        this.view = view;
    }

    @CallSuper
    public void detach(GrassrootView view) {
        this.view = null;
    }

    protected abstract void onViewAttached();
    protected abstract void onViewDetached();


    void handleException(Throwable t) {
        Log.e("ERROR", t.toString());
        // add toast & exit to main
    }

    void handleNetworkConnectionError(NetworkUnavailableException t) {
        Log.e("CONNECTION", t.toString());
        view.showConnectionFailedDialog().subscribe();
    }

    void handleAuthenticationError(AuthenticationInvalidException t) {
        Log.e("AUTHENTICATION", t.toString());
        view.showAuthenticationRecoveryDialog().subscribe();
    }

    void handleUnknownError(Exception e) {
        e.printStackTrace();
        Log.e("PRESENTER", "what is up with view? : = " + view);
        view.showErrorToast(R.string.error_unknown_generic);
    }

    void onNextButtonClick(){

    }

}
