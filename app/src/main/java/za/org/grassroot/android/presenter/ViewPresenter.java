package za.org.grassroot.android.presenter;

import za.org.grassroot.android.view.GrassrootView;

/**
 * Created by luke on 2017/08/11.
 */

public interface ViewPresenter extends GrassrootPresenter {

    void attach(GrassrootView view);
    void detach(GrassrootView view);
    void onViewCreated();

}
