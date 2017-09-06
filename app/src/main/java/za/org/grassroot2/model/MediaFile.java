package za.org.grassroot2.model;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import za.org.grassroot2.model.enums.NetworkEntityType;
import za.org.grassroot2.model.network.EntityForUpload;

/**
 * Created by luke on 2017/08/15.
 * Holds a record of a media file generated / stored for us (e.g., video/audio/image)
 */

public class MediaFile extends RealmObject implements EntityForUpload {

    public static final String FUNCTION_LIVEWIRE = "LIVEWIRE_MEDIA";

    @PrimaryKey
    private String uid;
    private String serverUid;

    // Realm cannot store URI, hence using String
    // And Android's FileProvider mechanism may be its most broken thing, so also storing own file
    private String absolutePath;
    private String contentProviderPath;
    private String mimeType;

    private boolean readyToUpload = false;
    private boolean compressOnSend = false;
    private boolean sendingUpstream = false;
    private boolean sentUpstream = false;

    private String mediaFunction;
    private String upstreamBucket; // will be set by server, and allows us to retrieve later, if we need (key will always be userUid + localUid)

    public MediaFile() {
        this.uid = UUID.randomUUID().toString();
    }


    public MediaFile(String localUri, String absolutePath, String mimeType, String mediaFunction) {
        this();
        this.contentProviderPath = localUri;
        this.absolutePath = absolutePath;
        this.mimeType = mimeType;
        this.mediaFunction = mediaFunction;
    }

    public String getUid() {
        return uid;
    }

    public String getServerUid() {
        return serverUid;
    }

    public void setServerUid(String serverUid) {
        this.serverUid = serverUid;
    }

    public String getContentProviderPath() {
        return contentProviderPath;
    }

    public void setContentProviderPath(String contentProviderPath) {
        this.contentProviderPath = contentProviderPath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isCompressOnSend() {
        return compressOnSend;
    }

    public void setCompressOnSend(boolean compressOnSend) {
        this.compressOnSend = compressOnSend;
    }

    public boolean isSentUpstream() {
        return sentUpstream;
    }

    public void setSentUpstream(boolean sentUpstream) {
        this.sentUpstream = sentUpstream;
    }

    public String getUpstreamBucket() {
        return upstreamBucket;
    }

    public void setUpstreamBucket(String upstreamBucket) {
        this.upstreamBucket = upstreamBucket;
    }

    public void setReadyToUpload(boolean readyToUpload) {
        this.readyToUpload = readyToUpload;
    }

    public String getMediaFunction() {
        return mediaFunction;
    }

    public void setMediaFunction(String mediaFunction) {
        this.mediaFunction = mediaFunction;
    }

    @Override
    public NetworkEntityType getType() {
        return NetworkEntityType.MEDIA_FILE;
    }

    @Override
    public void initUploading() {
        this.sendingUpstream = true;
    }

    @Override
    public void haltUploading(boolean uploadSucceeded) {
        this.sendingUpstream = false;
        if (!sentUpstream) {
            this.sentUpstream = uploadSucceeded;
        }
    }

    @Override
    public boolean isToUpload() {
        return readyToUpload && !sendingUpstream;
    }

    @Override
    public boolean isUploading() {
        return sendingUpstream;
    }

    @Override
    public boolean isUploaded() {
        return sentUpstream;
    }

    @Override
    public ArrayList<EntityForUpload> priorEntitiesToUpload() {
        return null;
    }
}