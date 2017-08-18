package za.org.grassroot.android.rxbinding;

import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by luke on 2017/07/10.
 */

public final class RxViewUtils {

    private static final String TAG = RxViewUtils.class.getSimpleName();

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

    public static Observable<Boolean> nullSafeMenuClick(MenuItem menuItem) {
        return menuItem == null ? Observable.just(false) :
                RxMenuItem.clicks(menuItem)
                        .map(new Function<Object, Boolean>() {
                            @Override
                            public Boolean apply(@NonNull Object o) throws Exception {
                                return true;
                            }
                        });
    }

}
