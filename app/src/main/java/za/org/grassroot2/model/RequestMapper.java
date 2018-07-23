package za.org.grassroot2.model;

import java.util.ArrayList;
import java.util.List;

import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.util.PhoneNumberFormatter;


public class RequestMapper {
    private static MemberRequest map(Contact c) {
        MemberRequest result = new MemberRequest();
        result.displayName = c.getDisplayName();
        result.emailAddress = c.getEmailAddresses().isEmpty() ? null : c.getEmailAddresses().get(0);
        result.alternateNumbers.addAll(c.getPhoneNumbers());
        result.phoneNumber = PhoneNumberFormatter.formatNumberToE164(c.getPhoneNumbers().get(0));
        result.createdDate = System.currentTimeMillis();
        return result;
    }

    public static List<MemberRequest> map(String groupUid, List<Contact> contacts) {
        List<MemberRequest> body = new ArrayList<>();
        if(contacts != null){
            for (Contact c : contacts) {
                MemberRequest request = map(c);
                request.groupUid = groupUid;
                body.add(request);
            }
        }
        return body;
    }
}
