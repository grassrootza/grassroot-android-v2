package za.org.grassroot2.view.fragment;

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;

import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot2.view.FragmentView;

/**
 * Created by luke on 2017/08/10.
 */

public class GrassrootFragment extends Fragment implements FragmentView {

    protected static final int ACTION_FRAGMENT_ATTACHED = 1;
    protected static final int ACTION_FRAGMENT_CREATED = 2;
    protected static final int ACTION_FRAGMENT_VIEW_CREATED = 3;

    protected Unbinder unbinder;
    protected PublishSubject<Integer> lifecyclePublisher = PublishSubject.create();

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        Timber.i("inside GrassrootFragment onDestroyView");
        if (this.unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public Observable<Integer> viewCreated() {
        return lifecyclePublisher
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer == ACTION_FRAGMENT_VIEW_CREATED;
                    }
                });
    }

}
