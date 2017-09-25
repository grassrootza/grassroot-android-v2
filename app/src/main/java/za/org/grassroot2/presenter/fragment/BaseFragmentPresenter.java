package za.org.grassroot2.presenter.fragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import za.org.grassroot2.view.FragmentView;


public abstract class BaseFragmentPresenter<T extends FragmentView> {

    protected T view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public abstract void onViewCreated();

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

}
