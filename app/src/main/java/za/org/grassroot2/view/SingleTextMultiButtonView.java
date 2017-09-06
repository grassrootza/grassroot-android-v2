package za.org.grassroot2.view;

import io.reactivex.Observable;
import za.org.grassroot2.model.dto.BtnReturnBundle;

/**
 * Created by luke on 2017/08/10.
 */

public interface SingleTextMultiButtonView extends SingleTextView {

    // when user hits next on main text input
    Observable<BtnReturnBundle> mainTextNext();

    // when user clicks either the big icon (eg for photo or actions)
    Observable<CharSequence> bigButtonClicked();

    // when user clicks one of three options under the text
    Observable<BtnReturnBundle> subtitleButtonClicked();

    // switch to one of the view pagers of text
    void scrollToPageOfSubtitles(int page);

}
