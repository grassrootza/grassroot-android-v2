package za.org.grassroot.android.model;

import java.util.ArrayList;

import io.reactivex.annotations.Nullable;

/**
 * Created by luke on 2017/08/15.
 * Interface to handle queueing and uploading
 */
public interface EntityForUpload {

    void initUploading();
    void haltUploading(boolean uploadSucceded);

    boolean isToUpload();
    boolean isUploading();
    boolean isUploaded();

    // to enforce a sequence : each entity is required to have a list of what is needed before it,
    // but those entities are responsible for maintaining their status as uploading or not
    // return null if nothing
    @Nullable ArrayList<EntityForUpload> priorEntitiesToUpload();

}
