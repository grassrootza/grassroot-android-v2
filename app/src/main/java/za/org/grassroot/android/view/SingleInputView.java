package za.org.grassroot.android.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/07/16.
 */

public interface  SingleInputView extends ProgressBarContainer {

    Observable<CharSequence> viewCreated();
    Observable<CharSequence> textInputChanged();
    Observable<CharSequence> textInputNextDone();
    Observable<CharSequence> textInputBackOther();

    void toggleNextDoneButton(boolean enabled);
    void toggleBackOtherButton(boolean enabled);

    void setInputDefault(CharSequence defaultValue);
    void displayErrorMessage(int messageRes);

    void setInputType(int type);
    void setImeOptions(int imeOptions);

    void focusOnInput();

}