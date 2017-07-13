package za.org.grassroot.android.presenter;

import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import za.org.grassroot.android.model.exception.AuthenticationInvalidException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;

/**
 * Created by luke on 2017/07/13.
 * todo: work out if can do this better via an operator
 */

public class RestSubscriber<T> implements SingleObserver<T> {

    Presenter presenter;
    SingleObserver<T> child;

    public RestSubscriber(Presenter presenter, SingleObserver<T> child) {
        this.presenter = presenter;
        this.child = child;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        child.onSubscribe(d);
    }

    @Override
    public void onSuccess(@NonNull T t) {
        child.onSuccess(t);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (e instanceof NetworkUnavailableException) {
            presenter.handleNetworkConnectionError((NetworkUnavailableException) e);
        } else if (e instanceof AuthenticationInvalidException) {
            presenter.handleAuthenticationError((AuthenticationInvalidException) e);
        } else {
            child.onError(e);
        }
    }
}
