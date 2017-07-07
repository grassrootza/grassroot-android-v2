package za.org.grassroot.android.presenter;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import za.org.grassroot.android.model.exception.InvalidViewForPresenterException;
import za.org.grassroot.android.model.exception.LifecycleOutOfSyncException;
import za.org.grassroot.android.view.GrassrootView;
import za.org.grassroot.android.view.LoginView;

/**
 * Created by luke on 2017/07/06.
 */

public class LoginPresenter extends Presenter {

    private LoginView view;

    private String userName;

    @Override
    public void attach(GrassrootView passedView) {
        try {
            view = (LoginView) passedView;
            subscriptions = new CompositeDisposable();
            onViewAttached();
        } catch (ClassCastException e) {
            handleException(new InvalidViewForPresenterException());
        }
    }

    @Override
    public void detach(GrassrootView view) {

    }

    @Override
    protected void onViewAttached() {
        try {
            subscriptions.add(view.usernameEntered().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {
                    validateUsernameAndRequestOtp(charSequence);
                }
            }));

            subscriptions.add(view.otpEntered().subscribe(new Consumer<CharSequence>() {
                @Override
                public void accept(@NonNull CharSequence charSequence) throws Exception {

                }
            }));
        } catch (NullPointerException e) {
            handleException(new LifecycleOutOfSyncException());
        }
    }

    @Override
    protected void onViewDetached() {
        try {
            subscriptions.dispose();
        } catch (NullPointerException e) {
            handleException(new LifecycleOutOfSyncException());
        }
    }

    private void validateUsernameAndRequestOtp(CharSequence charSequence) {
        // check if charsequence is valid & make a rest call
        view.requestOtpEntry();
    }

    private void validateOtpEntry(CharSequence charSequence) {
        // call the authentication service and check if these are okay, and if so, store and continue
    }
}
