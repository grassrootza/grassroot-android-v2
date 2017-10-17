package za.org.grassroot2.model.contact;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contact implements Serializable {

    private long id = -1;
    private String lookupId;

    private String displayName = "";

    private long   lastTimeContacted;
    private long   timesContacted;
    private String givenName;
    private String middleName;
    private String familyName;

    private List<String>  phoneNumbers;

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    private List<String> emailAddresses;
    private Uri          lookupUri;
    private boolean      hasEmails;
    private boolean      hasPhones;


    public Contact() {
        phoneNumbers = new ArrayList<>();
        emailAddresses = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : " ";
    }

    public void setPhoneNumber(String number) {
        phoneNumbers.add(number);
    }

    public void setEmailAddress(String email) {
        emailAddresses.add(email);
    }

    public String getLookupId() {
        return lookupId;
    }

    public void setLookupId(String lookupId) {
        this.lookupId = lookupId;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    public long getTimesContacted() {
        return timesContacted;
    }

    public void setTimesContacted(int timesContacted) {
        this.timesContacted = timesContacted;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Uri getLookupUri() {
        return lookupUri;
    }

    public void setLookupUri(Uri lookupUri) {
        this.lookupUri = lookupUri;
    }

    public void setHasEmails(boolean hasEmails) {
        this.hasEmails = hasEmails;
    }

    public boolean hasEmail() {
        return hasEmails;
    }

    public void setHasPhones(boolean hasPhones) {
        this.hasPhones = hasPhones;
    }

    public boolean hasPhoneNumber() {
        return hasPhones;
    }
}
