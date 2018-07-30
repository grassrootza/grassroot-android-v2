package za.org.grassroot2.services

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.webkit.MimeTypeMap
import io.reactivex.Single
import timber.log.Timber
import za.org.grassroot2.dagger.ApplicationContext
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.exception.FailedToCreateMediaFileException
import za.org.grassroot2.util.FileUtil
import za.org.grassroot2.util.ImageUtil
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
                        FileUtil.FILEPROVIDER_AUTHORITY,
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

    override fun captureMediaFile(mediaFileUid: String, targetImgWidth: Int, targetImgHeight: Int): Single<String> {
        return Single.create { e ->
            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, mediaFileUid)
            mediaFile!!.setReadyToUpload(true)
            if (mediaFile.mimeType.startsWith("image") && mediaFile.isCompressOnSend) {
                imageUtil.resizeImageFile(mediaFile.absolutePath, mediaFile.absolutePath, targetImgWidth, targetImgHeight)
            }
            e.onSuccess("DONE")
            MediaScannerConnection.scanFile(applicationContext, arrayOf(mediaFile.absolutePath), arrayOf(mediaFile.mimeType)) { path, uri -> Timber.d(path, uri.toString()) }
        }
    }

    override fun captureMediaFile(mediaFileUid: String): Single<String> {
        return this.captureMediaFile(mediaFileUid, DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT)
    }

    override fun storeGalleryFile(mediaFileUid: String, fileUri: Uri, targetImgWidth: Int, targetImgHeight: Int): Single<String> {
        return Single.create { e ->
            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, mediaFileUid)
            val localFileName = FileUtil.getLocalFileNameFromURI(fileUri, applicationContext)
            localFileName?.let {
                if (mediaFile!!.isCompressOnSend) {
                    imageUtil.resizeImageFile(it, mediaFile.absolutePath, targetImgWidth, targetImgHeight)
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
    }

    override fun storeGalleryFile(mediaFileUid: String, fileUri: Uri): Single<String> {
        return this.storeGalleryFile(mediaFileUid, fileUri, DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT)
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
    }
}