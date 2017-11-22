package za.org.grassroot2.services

import android.net.Uri
import io.reactivex.Single

/**
 * Created by luke on 2017/08/11.
 */

interface MediaService {

    /**
     *
     * @param mimeType The mime type of the desired image/file
     * @param mediaFunction What the image will be used for; passing null means we don't know yet
     * @return Emits the string of the MediaFile record
     */
    fun createFileForMedia(mimeType: String, mediaFunction: String): Single<String>
    fun storeMediaFile(fileName: String): Single<String>
    fun captureMediaFile(mediaFileUid: String): Single<String>
    fun captureMediaFile(mediaFileUid: String, targetImgWidth: Int, targetImgHeight: Int): Single<String>
    fun storeGalleryFile(mediaFileUid: String, fileUri: Uri): Single<String>
    fun storeGalleryFile(mediaFileUid: String, fileUri: Uri, targetImgWidth: Int, targetImgHeight: Int): Single<String>

}
