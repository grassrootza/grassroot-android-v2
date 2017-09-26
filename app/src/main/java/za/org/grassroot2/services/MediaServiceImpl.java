package za.org.grassroot2.services;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.exception.FailedToCreateMediaFileException;
import za.org.grassroot2.util.ImageUtil;

/**
 * Created by luke on 2017/08/11.
 */

public class MediaServiceImpl implements MediaService {

    private static final SimpleDateFormat IMG_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static final int DESIRED_COMPRESSED_WIDTH = 1280;
    public static final int DESIRED_COMPRESSED_HEIGHT = 720;
    public static final String FILEPROVIDER_AUTHORITY = "za.org.grassroot2.fileprovider";

    private final Context applicationContext;
    private final DatabaseService databaseService;
    private final NetworkService networkService;
    private final ImageUtil imageUtil;

    @Inject
    public MediaServiceImpl(@ApplicationContext Context applicationContext,
                            DatabaseService databaseService,
                            NetworkService networkService, ImageUtil imageUtil) {
        this.applicationContext = applicationContext;
        this.databaseService = databaseService;
        this.networkService = networkService;
        this.imageUtil = imageUtil;
    }

    @Override
    public Single<String> createFileForMedia(final String mimeType, final String mediaFunction) {
        return Single.create(e -> {
            try {
                File imageFile = createImageFile(mimeType);
                final Uri imageUri = FileProvider.getUriForFile(applicationContext,
                        FILEPROVIDER_AUTHORITY,
                        imageFile);
                Timber.d("taking image, URI = " + imageUri);
                MediaFile createdFile = databaseService.storeObject(MediaFile.class, new MediaFile(imageUri.toString(), imageFile.getAbsolutePath(), mimeType, mediaFunction));
                Timber.d("created media file = " + createdFile);
                final String createdUid = createdFile.getUid();
                e.onSuccess(createdUid);
            } catch (Throwable t) {
                Timber.e(t);
                throw new FailedToCreateMediaFileException();
            }
        });
    }

    private File createImageFile(final String mimeType) throws IOException {
        final String timeStamp = IMG_TIMESTAMP.format(new Date());
        final String extension = "." + (TextUtils.isEmpty(mimeType) ? "jpg" : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType));
        File storageDirectory = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
            Timber.e("Could not create picture folder");
        }
        return File.createTempFile(
                "IMG_" + timeStamp + "_",
                extension,
                storageDirectory
        );
    }

    @Override
    public Single<String> captureMediaFile(final String mediaFileUid, final boolean uploadNow) {
        return Single.create(e -> {
            MediaFile mediaFile = databaseService.loadObjectByUid(MediaFile.class, mediaFileUid);
            mediaFile.setReadyToUpload(true);
            if (mediaFile.isCompressOnSend()) {
                imageUtil.resizeImageFile(mediaFile.getAbsolutePath(), mediaFile.getAbsolutePath(), DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT);
            }
            e.onSuccess("DONE");
            if (uploadNow) {
                Timber.i("okay, trying to upload media file");
                networkService.uploadEntity(mediaFile, true)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
            MediaScannerConnection.scanFile(applicationContext, new String[] { mediaFile.getAbsolutePath() }, new String[] {mediaFile.getMimeType()}, (path, uri) -> Timber.d(path, uri.toString()));
//            triggerMediaScan(mediaFile.getAbsolutePath());
        });
    }

    @Override
    public Single<String> storeGalleryFile(final String mediaFileUid, final Uri fileUri, final boolean uploadNow) {
        return Single.create(e -> {
            final MediaFile mediaFile = databaseService.loadObjectByUid(MediaFile.class, mediaFileUid);
            String localFileName = getLocalFileNameFromURI(fileUri);
            if (mediaFile.isCompressOnSend()) {
                imageUtil.resizeImageFile(localFileName, mediaFile.getAbsolutePath(), DESIRED_COMPRESSED_WIDTH, DESIRED_COMPRESSED_HEIGHT);
            } else {
                mediaFile.setAbsolutePath(localFileName);
                mediaFile.setContentProviderPath(fileUri.toString());
            }
            mediaFile.setMimeType(getMimeType(fileUri));
            mediaFile.setReadyToUpload(true);
            e.onSuccess("DONE");
            databaseService.storeObject(MediaFile.class, mediaFile);
            if (uploadNow) {
                networkService.uploadEntity(mediaFile, true)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });
    }

    // todo: this doesn't work but Android has, being Android, managed to regress badly
    // the FileProvider mechanism is catastrophic, and makes it a real pain to expose
    // images to the gallery. Images are recorded but do not show up. Ah well.
    private void triggerMediaScan(final String filePath) {
        Timber.d("triggering media scan for file path: " + filePath);
        File f = new File(filePath);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f));
        applicationContext.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void loadImageIntoView(Uri imageUri, ImageView view) {
        // redo the below with Picasso, just warehousing for now

        /*String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        mediaPath = cursor.getString(columnIndex);
        imageView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
        // Set the Image in ImageView for Previewing the Media

        cursor.close();*/
    }

    private String getLocalFileNameFromURI(final Uri selectedImage) {
        String localImagePath = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = applicationContext.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst(); // if null, will throw error to subscriber, so check in here would be redundant
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            localImagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return localImagePath;
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(applicationContext.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        switch (extension) {
            case "jpg":
            case "jpeg":
                mimeType = "image/jpeg";
                break;
            case "png":
                mimeType = "image/png";
                break;
            default:
                break;
        }

        return mimeType;
    }
}