package za.org.grassroot.android.view;

import io.reactivex.Observable;

/**
 * Created by luke on 2017/08/10.
 */
public interface SingleTextView extends ProgressBarContainer {

    Observable<CharSequence> viewCreated();
    Observable<CharSequence> textInputChanged();

    void setInputDefault(CharSequence defaultValue);
    void displayErrorMessage(int messageRes);

    void setInputType(int type);
    void setImeOptions(int imeOptions);

    void focusOnInput();
}
