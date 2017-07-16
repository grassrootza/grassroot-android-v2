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

    void displayErrorMessage(int messageRes);

}