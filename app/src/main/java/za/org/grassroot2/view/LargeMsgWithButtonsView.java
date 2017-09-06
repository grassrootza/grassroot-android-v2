package za.org.grassroot2.view;

import io.reactivex.Observable;
import za.org.grassroot2.model.dto.BtnReturnBundle;

/**
 * Created by luke on 2017/08/25.
 */

public interface LargeMsgWithButtonsView {

    Observable<BtnReturnBundle> buttonClicked();

}
