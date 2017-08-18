package za.org.grassroot.android.presenter;

import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.view.MainView;

public interface MainPresenter extends LoggedInViewPresenter {

    void attach(MainView view);
    void menuReady();
    BtnGrouping obtainDefaultSubButtons();

}
