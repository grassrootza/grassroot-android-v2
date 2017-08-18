package za.org.grassroot.android.view;

import io.reactivex.Observable;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnReturnBundle;

public interface MainView extends LoggedInView {
    void setBtnGrouping(BtnGrouping btnGrouping);
    Observable<BtnReturnBundle> threeButtonRowButtonClicked();
}
