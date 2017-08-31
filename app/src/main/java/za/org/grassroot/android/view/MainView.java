package za.org.grassroot.android.view;

import io.reactivex.Observable;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import za.org.grassroot.android.model.SelectableItem;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnReturnBundle;

public interface MainView extends LoggedInView {

    Observable<BtnReturnBundle> mainTextNext();
    Observable<BtnReturnBundle> defaultRequestTextOrButtons(int headerString, Integer explanationRes,
                                                            boolean insideCreateCycle);

    Observable<CharSequence> requestTextInputNextCancel(int headerString, int explanationRes);

    Observable<Integer> requestLongDescriptionInput(int headerString, int inputHint);

    <T extends RealmObject & SelectableItem> Observable<String> requestSelection(int headerRes,
                                                                                 OrderedRealmCollection<T> items);

    Observable<Integer> requestConfirmationOrAction(int headerRes, String message, BtnGrouping btnGrouping, boolean allowSkip);

    Observable<BtnReturnBundle> btnGroupClicked();
    Observable<Object> skipButtonClicked();

    void setBtnGrouping(BtnGrouping btnGrouping);

    Observable<BtnReturnBundle> threeButtonRowButtonClicked();

}