package za.org.grassroot2.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.view.FragmentView;

/**
 * Created by luke on 2017/08/10.
 */

public abstract class GrassrootFragment extends Fragment implements FragmentView {

    protected static final int ACTION_FRAGMENT_ATTACHED = 1;
    protected static final int ACTION_FRAGMENT_CREATED = 2;
    protected static final int ACTION_FRAGMENT_VIEW_CREATED = 3;

    protected Unbinder unbinder;
    protected PublishSubject<Integer> lifecyclePublisher = PublishSubject.create();
    protected CompositeDisposable disposables = new CompositeDisposable();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onInject((GrassrootApplication)getActivity().getApplication());
    }

    protected abstract void onInject(GrassrootApplication application);

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        Timber.i("inside GrassrootFragment onDestroyView");
        disposables.clear();
        if (this.unbinder != null) {
            unbinder.unbind();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayoutResourceId() , container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @LayoutRes
    public abstract int getLayoutResourceId();


    @Override
    public Observable<Integer> viewCreated() {
        return lifecyclePublisher
                .filter(integer -> integer == ACTION_FRAGMENT_VIEW_CREATED);
    }

}
