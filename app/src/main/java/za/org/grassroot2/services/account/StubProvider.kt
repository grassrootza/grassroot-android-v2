package za.org.grassroot2.services.account

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

import timber.log.Timber

/**
 * Created by luke on 2017/08/18.
 */

class StubProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        Timber.e("creating stub provider")
        return false
    }

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings1: Array<String>?, s1: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int {
        return 0
    }
}
