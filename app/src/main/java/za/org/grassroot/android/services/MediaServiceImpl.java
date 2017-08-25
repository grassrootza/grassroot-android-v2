package za.org.grassroot.android.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot.android.dagger.ApplicationContext;
import za.org.grassroot.android.model.MediaFile;
import za.org.grassroot.android.model.exception.FailedToCreateMediaFileException;

/**
 * Created by luke on 2017/08/11.
 */

public class MediaServiceImpl implements MediaService {

    private static final SimpleDateFormat IMG_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String MEDIA_DIRECTORY = "/grassroot/";

    private final Context applicationContext;
    private final RealmService realmService;
    private final NetworkService networkService;

    @Inject
    public MediaServiceImpl(@ApplicationContext Context applicationContext,
                            RealmService realmService,
                            NetworkService networkService) {
        this.applicationContext = applicationContext;
        this.realmService = realmService;
        this.networkService = networkService;
    }

    @Override
    public Single<String> createFileForMedia(final String mimeType, final String mediaFunction) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                try {
                    File imageFile = createImageFile(mimeType);
                    final Uri imageUri = FileProvider.getUriForFile(applicationContext,
                            "za.org.grassroot.android.fileprovider",
                            imageFile);
                    Timber.d("taking image, URI = " + imageUri);
                    // could do this more elegantly, but, Android, Realm, threads
                    MediaFile createdFile = realmService.storeRealmObject(
                            new MediaFile(imageUri.toString(), imageFile.getAbsolutePath(), mimeType, mediaFunction), false);
                    Timber.d("created media file = " + createdFile);
                    final String createdUid = createdFile.getUid();
                    realmService.closeRealmOnThread();
                    e.onSuccess(createdUid);
                } catch (Throwable t) {
                    Timber.e(t);
                    throw new FailedToCreateMediaFileException();
                }
            }
        });
    }

    private File createImageFile(final String mimeType) throws IOException {
        final String timeStamp = IMG_TIMESTAMP.format(new Date());
        final String extension = "." + (TextUtils.isEmpty(mimeType) ? "jpg" : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType));
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                MediaFile mediaFile = realmService.loadObjectByUid(MediaFile.class, mediaFileUid, false);
                mediaFile.setReadyToUpload(true);
                e.onSuccess("DONE");
                if (uploadNow) {
                    Timber.i("okay, trying to upload media file");
                    networkService.uploadEntity(mediaFile, true)
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }
                triggerMediaScan(mediaFile.getContentProviderPath());
                realmService.closeRealmOnThread();
            }
        });
    }

    // todo: this doesn't work but Android has, being Android, managed to regress badly
    // the FileProvider mechanism is catastrophic, and makes it a real pain to expose
    // images to the gallery. Images are recorded but do not show up. Ah well.
    private void triggerMediaScan(final String filePath) {
        Timber.d("triggering media scan for file path: " + filePath);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
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
}