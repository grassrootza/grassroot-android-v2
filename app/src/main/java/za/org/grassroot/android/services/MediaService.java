package za.org.grassroot.android.services;

import android.net.Uri;
import android.widget.ImageView;

import io.reactivex.Single;

/**
 * Created by luke on 2017/08/11.
 */

public interface MediaService {

    /**
     *
     * @param mimeType The mime type of the desired image/file
     * @return Emits the string of the MediaFile record
     */
    Single<String> createFileForMedia(String mimeType);
    // todo: return type should state if need to prompt for compression etc
    Single<String> setMediaFileCaptured(String mediaFileUid);

    void loadImageIntoView(Uri imageUri, ImageView view);

}
