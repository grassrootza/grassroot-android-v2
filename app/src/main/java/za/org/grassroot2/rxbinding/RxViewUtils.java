package za.org.grassroot2.rxbinding;

import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * Created by luke on 2017/07/10.
 */

public final class RxViewUtils {

    private static final String TAG = RxViewUtils.class.getSimpleName();

    public static Predicate<Integer> imeNextDonePredicate() {
        return integer -> integer == EditorInfo.IME_ACTION_NEXT
                || integer == EditorInfo.IME_ACTION_DONE
                || integer == EditorInfo.IME_ACTION_GO;
    }

//    public static Observable<Boolean> nullSafeMenuClick(MenuItem menuItem) {
//        return menuItem == null ? Observable.just(false) :
//                RxMenuItem.clicks(menuItem)
//                        .map(o -> true);
//    }

    public static Observable<CharSequence> nullSafeTextViewNextDone(final TextView textView) {
        return textView == null ? Observable.<CharSequence>empty() :
                RxTextView
                        .editorActions(textView, imeNextDonePredicate())
                        .map(integer -> textView.getText());
    }

}
