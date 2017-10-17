package za.org.grassroot2.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/08/25.
 */

public interface FragmentView extends ProgressBarContainer{
    Observable<Integer> viewCreated();
    void showNoConnectionMessage();
    void handleNoConnection();
    void handleNoConnectionUpload();
}
