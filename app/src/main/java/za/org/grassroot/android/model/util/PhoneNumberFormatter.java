package za.org.grassroot.android.model.util;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luke on 2017/07/10.
 */

public final class PhoneNumberFormatter {

    private static final String TAG = PhoneNumberFormatter.class.getSimpleName();

    private static final Pattern nationalRegex = Pattern.compile("0[6,7,8]\\d{8}");
    private static final Pattern zaPhoneE164 = Pattern.compile("27[6,7,8]\\d{8}");
    private static final Pattern zaPhoneE164Plus = Pattern.compile("\\+27[6,7,8]\\d{8}"); // make the "+" more general

    // since google's libPhoneNumber is overkill for just this task, handrolling it
    public static String formatNumberToE164(String phoneNumber) {

        String normalizedNumber = PhoneNumberUtils.stripSeparators(phoneNumber);

        final Matcher alreadyCorrect = zaPhoneE164.matcher(normalizedNumber);

        if (alreadyCorrect.find()) {
            return normalizedNumber;
        } else if (zaPhoneE164Plus.matcher(normalizedNumber).find()) { // remove plus sign and return
            return normalizedNumber.substring(1);
        } else if (nationalRegex.matcher(normalizedNumber).find()) { // remove 0 and return
            return "27" + normalizedNumber.substring(1);
        } else {
            // throwing an error here would be good, but may lead to unintended crashes, and server
            // provides a further line of defence to malformed numbers, so just log and return
            Log.d(TAG, "error! tried to reformat, couldn't, here is phone number = " + normalizedNumber);
            return normalizedNumber;
        }
    }
}
