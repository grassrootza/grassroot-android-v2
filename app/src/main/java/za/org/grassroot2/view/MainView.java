package za.org.grassroot2.view;

import java.util.List;

import io.reactivex.Observable;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnReturnBundle;

public interface MainView extends LoggedInView {

    Observable<BtnReturnBundle> mainTextNext();
    Observable<BtnReturnBundle> defaultRequestTextOrButtons(int headerString, Integer explanationRes,
                                                            boolean insideCreateCycle);

    Observable<CharSequence> requestTextInputNextCancel(int headerString, int explanationRes);

    Observable<Integer> requestLongDescriptionInput(int headerString, int inputHint);

    <T extends SelectableItem> Observable<String> requestSelection(int headerRes, List<T> items);

    Observable<Integer> requestConfirmationOrAction(int headerRes, String message, BtnGrouping btnGrouping, boolean allowSkip);

    Observable<BtnReturnBundle> btnGroupClicked();
    Observable<Object> skipButtonClicked();

    void setBtnGrouping(BtnGrouping btnGrouping);

    Observable<BtnReturnBundle> threeButtonRowButtonClicked();

}