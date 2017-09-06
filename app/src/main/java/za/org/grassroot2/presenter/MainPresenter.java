package za.org.grassroot2.presenter;

import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.view.MainView;

public interface MainPresenter extends LoggedInViewPresenter {

    void attach(MainView view);
    void menuReady();
    BtnGrouping obtainMediaButtons();

}