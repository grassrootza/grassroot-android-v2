package za.org.grassroot.android.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/08/25.
 */

public interface FragmentView {

    Observable<Integer> viewCreated();

}
