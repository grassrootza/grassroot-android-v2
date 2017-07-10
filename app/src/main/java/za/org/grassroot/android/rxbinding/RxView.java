package za.org.grassroot.android.rxbinding;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.View;

import io.reactivex.Observable;

import static za.org.grassroot.android.rxbinding.Preconditions.checkNotNull;

/**
 * Ported by luke on 2017/07/10.
 */

public final class RxView {

    @CheckResult @NonNull
    public static Observable<Object> clicks(@NonNull View view) {
        checkNotNull(view, "view == null");
        return new ViewClickObservable(view);
    }

}
