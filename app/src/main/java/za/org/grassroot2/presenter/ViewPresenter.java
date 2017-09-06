package za.org.grassroot2.presenter;

import za.org.grassroot2.view.GrassrootView;

/**
 * Created by luke on 2017/08/11.
 */

public interface ViewPresenter extends GrassrootPresenter {

    void attach(GrassrootView view);
    void detach(GrassrootView view);
    void onViewCreated();

}
