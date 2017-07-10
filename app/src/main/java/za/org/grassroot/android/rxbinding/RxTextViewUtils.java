package za.org.grassroot.android.rxbinding;

import android.view.inputmethod.EditorInfo;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;

/**
 * Created by luke on 2017/07/10.
 */

public final class RxTextViewUtils {

    private static final String TAG = RxTextViewUtils.class.getSimpleName();

    public static Predicate<Integer> imeNextDonePredicate() {
        return new Predicate<Integer>() {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
                return integer == EditorInfo.IME_ACTION_NEXT
                        || integer == EditorInfo.IME_ACTION_DONE
                        || integer == EditorInfo.IME_ACTION_GO;
            }
        };
    }

}
