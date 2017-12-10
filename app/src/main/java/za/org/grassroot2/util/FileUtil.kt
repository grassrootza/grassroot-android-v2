package za.org.grassroot2.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

/**
 * Created by luke on 2017/12/05.
 */
object FileUtil {

    val FILEPROVIDER_AUTHORITY = "za.org.grassroot2.fileprovider"

    // todo: continue moving stuff in here
    fun getLocalFileNameFromURI(selectedImage: Uri, applicationContext: Context): String? {
        var localImagePath: String? = null
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = applicationContext.contentResolver.query(selectedImage, filePathColumn, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst() // if null, will throw error to subscriber, so check in here would be redundant
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            localImagePath = cursor.getString(columnIndex)
            cursor.close()
        }
        return localImagePath
    }

}