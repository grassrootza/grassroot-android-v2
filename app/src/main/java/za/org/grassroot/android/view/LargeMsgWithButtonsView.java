package za.org.grassroot.android.view;

import io.reactivex.Observable;
import za.org.grassroot.android.model.dto.BtnReturnBundle;

/**
 * Created by luke on 2017/08/25.
 */

public interface LargeMsgWithButtonsView {

    Observable<BtnReturnBundle> buttonClicked();

}
