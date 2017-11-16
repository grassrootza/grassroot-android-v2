package za.org.grassroot2.services

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.webkit.MimeTypeMap
import android.widget.ImageView

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

import javax.inject.Inject

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.dagger.ApplicationContext
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.exception.FailedToCreateMediaFileException
import za.org.grassroot2.util.ImageUtil

/**
 * Created by luke on 2017/08/11.
 */

class MediaServiceImpl @Inject
constructor(@param:ApplicationContext private val applicationContext: Context,
            private val databaseService: DatabaseService,
            private val imageUtil: ImageUtil) : MediaService {


    override fun storeMediaFile(fileName: String): Single<String> {
        return Single.create { e ->
            try {
                val mediaFile = MediaFile(fileName, fileName, "", MediaFile.FUNCTION_LIVEWIRE)
                val stored = databaseService.storeObject(MediaFile::class.javaObjectType, mediaFile)
                e.onSuccess(stored.uid)
            } catch (t: Exception) {
                t.printStackTrace()
                e.onError(t)
            }
        }
    }

    override fun createFileForMedia(mimeType: String, mediaFunction: String): Single<String> {
        return Single.create { e ->
            try {
                val imageFile = createImageFile(mimeType)
                val imageUri = FileProvider.getUriForFile(applicationContext,
                        FILEPROVIDER_AUTHORITY,
                        imageFile)
                Timber.d("taking image, URI = " + imageUri)
                val toSave = MediaFile(imageUri.toString(), imageFile.absolutePath, mimeType, mediaFunction)
                toSave.isCompressOnSend = true
                val createdFile = databaseService.storeObject(MediaFile::class.java, toSave)
                Timber.d("created media file = " + createdFile)
                val createdUid = createdFile.uid
                e.onSuccess(createdUid)
            } catch (t: Throwable) {
                Timber.e(t)
                throw FailedToCreateMediaFileException()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(mimeType: String): File {
        val timeStamp = IMG_TIMESTAMP.format(Date())
        val extension = "." + if (TextUtils.isEmpty(mimeType)) "jpg" else MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val storageDirectory = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDirectory!!.exists() && !storageDirectory.mkdirs()) {
            Timber.e("Could not create picture folder")
        }
        return File.createTempFile(
                "IMG_" + timeStamp + "_",
                extension,
                storageDirectory
        )
    }

    override fun captureMediaFile(mediaFileUid: String): Single<String> {
        return Single.create { e ->
            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, mediaFileUid)
            mediaFile!!.setReadyToUpload(true)
            if (mediaFile.mimeType.startsWith("image") && mediaFile.isCompressOnSend) {
                imageUtil.resizeImageFile(mediaFile.absolutePath, mediaFile.absolutePath, DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT)
            }
            e.onSuccess("DONE")
            MediaScannerConnection.scanFile(applicationContext, arrayOf(mediaFile.absolutePath), arrayOf(mediaFile.mimeType)) { path, uri -> Timber.d(path, uri.toString()) }
        }
    }

    override fun storeGalleryFile(mediaFileUid: String, fileUri: Uri): Single<String> {
        return Single.create { e ->
            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, mediaFileUid)
            val localFileName = getLocalFileNameFromURI(fileUri)
            if (mediaFile!!.isCompressOnSend) {
                imageUtil.resizeImageFile(localFileName, mediaFile.absolutePath, DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT)
            } else {
                mediaFile.absolutePath = localFileName
                mediaFile.contentProviderPath = fileUri.toString()
            }
            mediaFile.mimeType = getMimeType(fileUri)
            mediaFile.setReadyToUpload(true)
            e.onSuccess("DONE")
            databaseService.storeObject(MediaFile::class.java, mediaFile)
        }
    }

    private fun getLocalFileNameFromURI(selectedImage: Uri): String? {
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

    private fun getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        val extension: String
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(applicationContext.contentResolver.getType(uri))
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
        when (extension) {
            "jpg", "jpeg" -> mimeType = "image/jpeg"
            "png" -> mimeType = "image/png"
            else -> {
            }
        }

        return mimeType
    }

    companion object {

        private val IMG_TIMESTAMP = SimpleDateFormat("yyyyMMdd_HHmmss")
        val DESIRED_COMPRESSED_WIDTH = 1280
        val DESIRED_COMPRESSED_HEIGHT = 720
        val FILEPROVIDER_AUTHORITY = "za.org.grassroot2.fileprovider"
    }
}