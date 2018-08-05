package za.org.grassroot2.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.*
import android.text.TextUtils
import io.reactivex.Observable
import za.org.grassroot2.model.contact.Contact
import java.util.*
import javax.inject.Inject


class ContactHelper @Inject
constructor(c: Context) {

    private val cr: ContentResolver = c.contentResolver

    val allContactsSimple: Observable<List<Contact>>
        get() = Observable.fromCallable {
            val result = ArrayList<Contact>()
            val cursor = cr.query(Contacts.CONTENT_URI, sContactColumns, null, null, Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC")
            val includedIds = contactIdsWithPhone
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
                        if (includedIds.contains(contactId)) {
                            result.add(modelFromCursorSimple(cursor))
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            result
        }

    val allContacts: Observable<List<Contact>>
        get() = Observable.fromCallable {
            val result = ArrayList<Contact>()
            val cursor = cr.query(Contacts.CONTENT_URI, sContactColumns, null, null, Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC")
            val includedIds = contactIdsWithPhone
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID))
                        if (includedIds.contains(contactId)) {
                            result.add(getContact(contactId))
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            result
        }

    private val contactIdsWithEmail: Set<Long>
        get() {
            val includedIds = HashSet<Long>()
            val cursor = cr.query(Data.CONTENT_URI, sSimpleContactProjection, Data.MIMETYPE + "=?",
                    arrayOf(CommonDataKinds.Email.CONTENT_ITEM_TYPE), null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        includedIds.add(cursor.getLong(cursor.getColumnIndex(RawContacts.CONTACT_ID)))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return includedIds
        }

    private val contactIdsWithPhone: Set<Long>
        get() {
            val includedIds = HashSet<Long>()
            val cursor: Cursor? = cr.query(Data.CONTENT_URI, sSimpleContactProjection, Data.MIMETYPE + "=?",
                    arrayOf(CommonDataKinds.Phone.CONTENT_ITEM_TYPE), null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        includedIds.add(cursor.getLong(cursor.getColumnIndex(RawContacts.CONTACT_ID)))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            return includedIds
        }

    fun getContact(contactId: Long): Contact {
        val result = Contact()
        val c = cr.query(Contacts.CONTENT_URI.buildUpon().appendPath(contactId.toString()).build(), sContactColumns, null, null, null)
        if (c != null) {
            if (c.moveToFirst()) {
                setCommonContactData(result, c)
            }
            c.close()
            val data = cr.query(Data.CONTENT_URI, fullDataProjection, Data.CONTACT_ID + "=?", arrayOf(result.id.toString()), null)
            if (data != null) {
                if (data.moveToFirst()) {
                    do {
                        parseData(data, result)
                    } while (data.moveToNext())
                }
                data.close()
            }
        }
        return result
    }

    fun modelFromCursorSimple(c: Cursor): Contact {
        val cm = Contact()
        cm.id = c.getLong(c.getColumnIndex(Contacts._ID))
        cm.lookupId = c.getString(c
                .getColumnIndex(Contacts.LOOKUP_KEY))
        cm.displayName = c.getString(c
                .getColumnIndex(Contacts.DISPLAY_NAME))
        cm.setHasPhones(c.getInt(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 1)
        return cm
    }

    fun modelFromCursor(cursor: Cursor?): Contact? {
        var cm: Contact? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cm = Contact()
                setCommonContactData(cm, cursor)
                do {
                    parseData(cursor, cm)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return cm
    }

    private fun parseData(cursor: Cursor, cm: Contact) {
        val mimeType = cursor.getString(cursor.getColumnIndex(Contacts.Entity.MIMETYPE))
        if (!TextUtils.isEmpty(mimeType)) {
            when (mimeType) {
                CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> cm.setPhoneNumber(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)))
                CommonDataKinds.Email.CONTENT_ITEM_TYPE -> cm.setEmailAddress(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS)))
                CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                    cm.givenName = cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME))
                    cm.middleName = cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.MIDDLE_NAME))
                    cm.familyName = cursor.getString(cursor.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME))
                }
                else -> {
                }
            }
        }
    }

    private fun setCommonContactData(cm: Contact, c: Cursor) {
        cm.displayName = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME))
        cm.id = c.getLong(c.getColumnIndex(Contacts._ID))
        cm.lookupId = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY))
    }

    companion object {

        private val sSimpleContactProjection = arrayOf(RawContacts.CONTACT_ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME, Contacts.LAST_TIME_CONTACTED, Contacts.Entity.STARRED, Contacts.HAS_PHONE_NUMBER, Contacts.Data.MIMETYPE, Contacts.Data.DATA2, Contacts.Data.DATA3, Contacts.Data.DATA5, Data.RAW_CONTACT_ID)

        private val sContactColumns = arrayOf(Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER)

        private val fullDataProjection = arrayOf(Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11, Data.DATA12, Data.DATA13, Data.DATA14, Data.DATA15, Data.SYNC1, Data.SYNC2, Data.SYNC3, Data.SYNC4, Data.DATA_VERSION, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY, Data.MIMETYPE)
    }

}
