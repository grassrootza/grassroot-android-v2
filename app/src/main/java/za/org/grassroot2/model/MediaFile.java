package za.org.grassroot2.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.UUID;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForUpload;
import za.org.grassroot2.model.network.Syncable;

/**
 * Created by luke on 2017/08/15.
 * Holds a record of a media file generated / stored for us (e.g., video/audio/image)
 */

@DatabaseTable(tableName = "media_files")
public class MediaFile implements EntityForUpload, Syncable {

    public static final String FUNCTION_LIVEWIRE = "LIVEWIRE_MEDIA";
    public static final String FUNCTION_USER_PROFILE_PHOTO = "USER_PROFILE_PHOTO";
    public static final String FUNCTION_GROUP_PROFILE_PHOTO = "GROUP_PROFILE_PHOTO";

    @DatabaseField(id = true)
    private String uid;
    @DatabaseField
    private String serverUid;

    @DatabaseField
    private String absolutePath;
    @DatabaseField
    private String contentProviderPath;
    @DatabaseField
    private String mimeType;

    @DatabaseField
    private boolean readyToUpload = false;
    @DatabaseField
    private boolean compressOnSend = false;
    @DatabaseField
    private boolean sendingUpstream = false;
    @DatabaseField
    private boolean sentUpstream = false;

    @DatabaseField
    private String mediaFunction;
    @DatabaseField
    private String upstreamBucket; // will be set by server, and allows us to retrieve later, if we need (key will always be userUid + localUid)

    @DatabaseField
    private transient long createdDate;

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
    public GrassrootEntityType getType() {
        return GrassrootEntityType.MEDIA_FILE;
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

    @Override
    public boolean isSynced() {
        return sentUpstream;
    }

    @Override
    public long createdDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}
