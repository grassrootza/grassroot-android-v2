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
     * @param mediaFunction What the image will be used for; passing null means we don't know yet
     * @return Emits the string of the MediaFile record
     */
    Single<String> createFileForMedia(String mimeType, String mediaFunction);
    // todo: return type should state if need to prompt for compression etc
    Single<String> captureMediaFile(String mediaFileUid, boolean uploadNow);

    void loadImageIntoView(Uri imageUri, ImageView view);

}
