package za.org.grassroot.android.view;

import io.reactivex.Observable;
import za.org.grassroot.android.model.dto.BtnReturnBundle;

/**
 * Created by luke on 2017/08/10.
 */
public interface SingleTextView extends FragmentView {

    Observable<CharSequence> textInputChanged();
    Observable<BtnReturnBundle> mainTextNext();

    void setInputDefault(CharSequence defaultValue);
    void displayErrorMessage(int messageRes);

    void setInputType(int type);
    void setImeOptions(int imeOptions);

    void focusOnInput();
}
