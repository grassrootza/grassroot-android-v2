package za.org.grassroot2.model;

import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.request.MemberRequestObject;
import za.org.grassroot2.model.util.PhoneNumberFormatter;


public class RequestMapper {
    public static MemberRequestObject map(Contact c) {
        MemberRequestObject result = new MemberRequestObject();
        result.displayName = c.getDisplayName();
        result.emailAddress = c.getEmailAddresses().isEmpty() ? null : c.getEmailAddresses().get(0);
        result.alternateNumbers.addAll(c.getPhoneNumbers());
        result.memberMsisdn = PhoneNumberFormatter.formatNumberToE164(c.getPhoneNumbers().get(0));
        return result;
    }
}
