package za.org.grassroot.android.model.util;

import android.telephony.PhoneNumberUtils;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.org.grassroot.android.model.exception.InvalidPhoneNumberException;

public final class PhoneNumberUtil {

    private static final Pattern zaPhoneE164 = Pattern.compile("27[6,7,8]\\d{8}");
    private static final Pattern zaPhoneE164Plus = Pattern.compile("\\+27[6,7,8]\\d{8}"); // make the "+" more general
    private static final Pattern nationalRegex = Pattern.compile("0[6,7,8]\\d{8}");

    public static boolean isPossibleNumber(CharSequence sequence) {
        return sequence.length() >= 10 &&
                Patterns.PHONE.matcher(sequence).matches();
    }

    public static String convertToMsisdn(CharSequence phoneNumber) {
        String normalizedNumber = PhoneNumberUtils.stripSeparators("" + phoneNumber);
        final Matcher alreadyCorrect = zaPhoneE164.matcher(normalizedNumber);
        if (alreadyCorrect.find()) {
            return normalizedNumber;
        } else if (zaPhoneE164Plus.matcher(normalizedNumber).find()) { // remove plus sign and return
            return normalizedNumber.substring(1);
        } else if (nationalRegex.matcher(normalizedNumber).find()) { // remove 0 and return
            return "27" + normalizedNumber.substring(1);
        } else {
            throw new InvalidPhoneNumberException();
        }
    }

}
