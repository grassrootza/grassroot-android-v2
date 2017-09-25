package za.org.grassroot2.services.rest;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import za.org.grassroot2.model.exception.AuthenticationInvalidException;
import za.org.grassroot2.model.exception.NetworkUnavailableException;
import za.org.grassroot2.model.exception.ServerUnreachableException;
import za.org.grassroot2.presenter.GrassrootPresenter;
import za.org.grassroot2.presenter.BasePresenter;

/**
 * TODO: work out if can do this better via an operator
 */
public class RestSubscriber<T> implements Observer<T> {

    private GrassrootPresenter presenter;
    private SingleObserver<T> child;

    public RestSubscriber(BasePresenter presenter, SingleObserver<T> child) {
        this.presenter = presenter;
        this.child = child;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        child.onSubscribe(d);
    }

    @Override
    public void onNext(T t) {
        child.onSuccess(t);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (e instanceof NetworkUnavailableException) {
            presenter.handleNetworkConnectionError((NetworkUnavailableException) e);
        } else if (e instanceof AuthenticationInvalidException) {
            presenter.handleAuthenticationError((AuthenticationInvalidException) e);
        } else if (e instanceof ServerUnreachableException) {
            presenter.handleServerUnreachableError((ServerUnreachableException) e);
        } else {
            child.onError(e);
        }
    }

    @Override
    public void onComplete() {

    }
}
