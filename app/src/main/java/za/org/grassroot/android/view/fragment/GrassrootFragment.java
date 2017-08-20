package za.org.grassroot.android.view.fragment;

import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.Unbinder;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import za.org.grassroot.android.R;

/**
 * Created by luke on 2017/08/10.
 */

public class GrassrootFragment extends Fragment {

    protected static final int ACTION_FRAGMENT_ATTACHED = 1;
    protected static final int ACTION_FRAGMENT_CREATED = 2;
    protected static final int ACTION_FRAGMENT_VIEW_CREATED = 3;

    protected Unbinder unbinder;
    protected PublishSubject<Integer> lifecyclePublisher = PublishSubject.create();

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        Timber.i("inside GrassrootFragment onDestroyView");
        if (this.unbinder != null) {
            unbinder.unbind();
        }
    }

    public void showProgressBar() {
        safeToggleProgressBar(progressBar, true);
    }

    public void closeProgressBar() {
        safeToggleProgressBar(progressBar, false);
    }

    protected void safeToggleProgressBar(ProgressBar progressBar, boolean shown) {
        if (progressBar != null) {
            progressBar.setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

}
