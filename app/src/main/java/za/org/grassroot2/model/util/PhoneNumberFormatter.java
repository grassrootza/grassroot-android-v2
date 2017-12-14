package za.org.grassroot2.model.util;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

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
            Timber.e("error! tried to reformat, couldn't, here is phone number = %s", normalizedNumber);
            return normalizedNumber;
        }
    }

    // note : this will need to be changed for international format numbers, but using it for now
    public static String formatNumberForDisplay(String storedNumber, String joinString) {
        String prefix = "0" + storedNumber.substring(2, 4);
        String midnumbers, finalnumbers;
        try {
            midnumbers = storedNumber.substring(4, 7);
            finalnumbers = storedNumber.substring(7, 11);
        } catch (Exception e) { // in case the string doesn't have enough digits ...
            midnumbers = storedNumber.substring(4);
            finalnumbers = "";
        }
        return TextUtils.join(joinString, Arrays.asList(prefix, midnumbers, finalnumbers));
    }
}
