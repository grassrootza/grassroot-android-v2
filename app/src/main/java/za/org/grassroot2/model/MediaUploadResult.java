package za.org.grassroot2.model;

public class MediaUploadResult {
    private String mediaFileKey;
    private String mediaFileBucket;

    private String imageUrl;

    private String mediaRecordUid;
    private String mediaFunction;

    public MediaUploadResult() {
    }

    public String getMediaFileKey() {
        return mediaFileKey;
    }

    public void setMediaFileKey(String mediaFileKey) {
        this.mediaFileKey = mediaFileKey;
    }

    public String getMediaFileBucket() {
        return mediaFileBucket;
    }

    public void setMediaFileBucket(String mediaFileBucket) {
        this.mediaFileBucket = mediaFileBucket;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMediaRecordUid() {
        return mediaRecordUid;
    }

    public void setMediaRecordUid(String mediaRecordUid) {
        this.mediaRecordUid = mediaRecordUid;
    }

    public String getMediaFunction() {
        return mediaFunction;
    }

    public void setMediaFunction(String mediaFunction) {
        this.mediaFunction = mediaFunction;
    }
}
