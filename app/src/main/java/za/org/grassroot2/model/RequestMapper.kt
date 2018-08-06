package za.org.grassroot2.model

import java.util.ArrayList

import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.util.PhoneNumberFormatter


object RequestMapper {
    private fun map(c: Contact): MemberRequest {
        val result = MemberRequest()
        result.displayName = c.displayName
        result.emailAddress = if (c.emailAddresses.isEmpty()) null else c.emailAddresses[0]
        result.alternateNumbers.addAll(c.phoneNumbers)
        result.phoneNumber = PhoneNumberFormatter.formatNumberToE164(c.phoneNumbers[0])
        result.createdDate = System.currentTimeMillis()
        return result
    }

    fun map(groupUid: String, contacts: List<Contact>?): List<MemberRequest> {
        val body = ArrayList<MemberRequest>()
        if (contacts != null) {
            for (c in contacts) {
                val request = map(c)
                request.groupUid = groupUid
                body.add(request)
            }
        }
        return body
    }
}
