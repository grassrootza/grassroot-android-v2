package za.org.grassroot2.presenter.fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;
import za.org.grassroot2.util.UserPreference;
import za.org.grassroot2.view.FragmentView;


public abstract class BaseFragmentPresenter<T extends FragmentView> {

    protected T view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public abstract void onViewCreated();

    public void attach(T view) {
        this.view = view;
        EventBus.getDefault().register(this);
    }

    public void detach(T view) {
        disposables.clear();
        EventBus.getDefault().unregister(this);
        this.view = null;
    }

    protected void disposableOnDetach(Disposable d) {
        if (!disposables.isDisposed()) {
            disposables.add(d);
        }
    }

    @Subscribe
    void emptyEvent(Object o) {}

    public void handleNetworkConnectionError(Throwable t) {
        Timber.d(t);
        view.handleNoConnection();
    }

    public void handleNetworkUploadError(Throwable t) {
        Timber.d(t);
        view.handleNoConnectionUpload();
    }

}
