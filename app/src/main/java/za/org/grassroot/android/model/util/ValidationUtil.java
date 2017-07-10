package za.org.grassroot.android.model.util;

import android.telephony.PhoneNumberUtils;
import android.util.Patterns;

/**
 * Created by luke on 2017/07/10.
 */

public final class ValidationUtil {

    public static boolean isPossibleNumber(CharSequence sequence) {
        return sequence.length() >= 10 &&
                Patterns.PHONE.matcher(sequence).matches();
    }

}
