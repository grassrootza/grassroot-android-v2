package za.org.grassroot2.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import za.org.grassroot2.model.contact.Contact;


public class ContactHelper {

    private final ContentResolver cr;

    @Inject
    public ContactHelper(Context c) {
        cr = c.getContentResolver();
    }

    private static String[] sSimpleContactProjection = new String[]{
            RawContacts.CONTACT_ID,
            Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME,
            Contacts.LAST_TIME_CONTACTED,
            Contacts.Entity.STARRED,
            Contacts.HAS_PHONE_NUMBER,
            Contacts.Data.MIMETYPE,
            Contacts.Data.DATA2,
            Contacts.Data.DATA3,
            Contacts.Data.DATA5,
            Data.RAW_CONTACT_ID
    };

    public static final String HAS_EMAIL_ADDRESS = "has_email_address";

    private static String[] sContactColumns = new String[]{
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.DISPLAY_NAME,
            Contacts.HAS_PHONE_NUMBER};

    private static String[] fullDataProjection = new String[]{
            Data.DATA1, Data.DATA2, Data.DATA3,
            Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8,
            Data.DATA9, Data.DATA10, Data.DATA11, Data.DATA12, Data.DATA13,
            Data.DATA14, Data.DATA15, Data.SYNC1, Data.SYNC2, Data.SYNC3,
            Data.SYNC4, Data.DATA_VERSION, Data.IS_PRIMARY,
            Data.IS_SUPER_PRIMARY, Data.MIMETYPE};

    public Contact getContact(long contactId) {
        Contact result = new Contact();
        Cursor c = cr.query(Contacts.CONTENT_URI.buildUpon().appendPath(String.valueOf(contactId)).build(), sContactColumns, null, null,
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                setCommonContactData(result, c);
            }
            c.close();
            Cursor data = cr.query(Data.CONTENT_URI, fullDataProjection, Data.CONTACT_ID + "=?", new String[]{String.valueOf(result.getId())},
                    null);
            if (data != null) {
                if (data.moveToFirst()) {
                    do {
                        parseData(data, result);
                    } while (data.moveToNext());
                }
                data.close();
            }
        }
        return result;
    }

    public Observable<List<Contact>> getAllContactsSimple() {
        return Observable.fromCallable(() -> {
            List<Contact> result = new ArrayList<>();
            Cursor cursor = cr.query(Contacts.CONTENT_URI, sContactColumns, null, null, Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
            Set<Long> includedIds = getContactIdsWithPhone();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
                        if (includedIds.contains(contactId)) {
                            result.add(modelFromCursorSimple(cursor));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return result;
        });
    }

    public Observable<List<Contact>> getAllContacts() {
        return Observable.fromCallable(() -> {
            List<Contact> result = new ArrayList<>();
            Cursor cursor = cr.query(Contacts.CONTENT_URI, sContactColumns, null, null, Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
            Set<Long> includedIds = getContactIdsWithPhone();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
                        if (includedIds.contains(contactId)) {
                            result.add(getContact(contactId));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return result;
        });
    }

    @NonNull
    private Set<Long> getContactIdsWithEmail() {
        Set<Long> includedIds = new HashSet<>();
        Cursor cursor = cr.query(Data.CONTENT_URI, sSimpleContactProjection, Data.MIMETYPE + "=?",
                new String[]{CommonDataKinds.Email.CONTENT_ITEM_TYPE}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    includedIds.add(cursor.getLong(cursor.getColumnIndex(RawContacts.CONTACT_ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return includedIds;
    }

    @NonNull
    private Set<Long> getContactIdsWithPhone() {
        Set<Long> includedIds = new HashSet<>();
        Cursor cursor;
        cursor = cr.query(Data.CONTENT_URI, sSimpleContactProjection, Data.MIMETYPE + "=?",
                new String[]{CommonDataKinds.Phone.CONTENT_ITEM_TYPE}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    includedIds.add(cursor.getLong(cursor.getColumnIndex(RawContacts.CONTACT_ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return includedIds;
    }

    public Contact modelFromCursorSimple(Cursor c) {
        Contact cm = new Contact();
        cm.setId(c.getLong(c.getColumnIndex(Contacts._ID)));
        cm.setLookupId(c.getString(c
                .getColumnIndex(Contacts.LOOKUP_KEY)));
        cm.setDisplayName(c.getString(c
                .getColumnIndex(Contacts.DISPLAY_NAME)));
        cm.setHasPhones(c.getInt(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 1);
        return cm;
    }

    public Contact modelFromCursor(Cursor cursor) {
        Contact cm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cm = new Contact();
                setCommonContactData(cm, cursor);
                do {
                    parseData(cursor, cm);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cm;
    }

    private void parseData(Cursor cursor, Contact cm) {
        String mimeType = cursor.getString(cursor.getColumnIndex(Contacts.Entity.MIMETYPE));
        if (!TextUtils.isEmpty(mimeType)) {
            switch (mimeType) {
                case CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                    cm.setPhoneNumber(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)));
                    break;
                case CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                    cm.setEmailAddress(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS)));
                    break;
                case CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                    cm.setGivenName(cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME)));
                    cm.setMiddleName(cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.MIDDLE_NAME)));
                    cm.setFamilyName(cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME)));
                    break;
                default:
                    break;
            }
        }
    }

    private void setCommonContactData(Contact cm, Cursor c) {
        cm.setDisplayName(c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME)));
        cm.setId(c.getLong(c.getColumnIndex(Contacts._ID)));
        cm.setLookupId(c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY)));
    }

}
