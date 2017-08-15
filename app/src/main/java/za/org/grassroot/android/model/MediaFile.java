package za.org.grassroot.android.model;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 2017/08/15.
 * Holds a record of a media file generated / stored for us (e.g., video/audio/image)
 */

public class MediaFile extends RealmObject implements EntityForUpload {

    @PrimaryKey
    private String uid;

    // Realm cannot store URI, hence using String
    private String localPath;
    private String mimeType;

    private boolean readyToUpload = false;
    private boolean compressOnSend = false;
    private boolean sendingUpstream = false;
    private boolean sentUpstream = false;
    private String upstreamUrl;

    public MediaFile() {
        this.uid = UUID.randomUUID().toString();
    }

    public MediaFile(String localUri, String mimeType) {
        this();
        this.localPath = localUri;
        this.mimeType = mimeType;
    }

    public String getUid() {
        return uid;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
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

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    public void setReadyToUpload(boolean readyToUpload) {
        this.readyToUpload = readyToUpload;
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
