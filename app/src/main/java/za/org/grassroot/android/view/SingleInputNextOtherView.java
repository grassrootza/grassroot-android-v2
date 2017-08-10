package za.org.grassroot.android.view;

import io.reactivex.Observable;

public interface SingleInputNextOtherView extends SingleTextView {

    void toggleNextDoneButton(boolean enabled);
    void toggleBackOtherButton(boolean enabled);
    Observable<CharSequence> textInputNextDone();
    Observable<CharSequence> textInputBackOther();

}