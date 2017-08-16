package za.org.grassroot.android.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;
import za.org.grassroot.android.model.EntityForUpload;
import za.org.grassroot.android.model.LiveWireAlert;
import za.org.grassroot.android.model.MediaFile;
import za.org.grassroot.android.model.UploadResult;
import za.org.grassroot.android.model.enums.UploadableEntityType;
import za.org.grassroot.android.model.exception.EntityAlreadyUploadingException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.exception.ServerErrorException;
import za.org.grassroot.android.services.rest.GrassrootUserApi;
import za.org.grassroot.android.services.rest.RestResponse;

/**
 * Created by luke on 2017/08/16.
 */

public class NetworkServiceImpl implements NetworkService {

    private final UserDetailsService userDetailsService;
    private final GrassrootUserApi grassrootUserApi;

    private String currentUserUid; // given frequency of calling/using

    @Inject
    public NetworkServiceImpl(UserDetailsService userDetailsService, GrassrootUserApi grassrootUserApi) {
        this.userDetailsService = userDetailsService;
        this.grassrootUserApi = grassrootUserApi;
    }

    @Override
    public Observable<UploadResult> uploadEntity(final EntityForUpload entityForUpload, final boolean forceEvenIfPriorUploaded) {
        return Observable.create(new ObservableOnSubscribe<UploadResult>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<UploadResult> e) throws Exception {
                currentUserUid = userDetailsService.getCurrentUserUid();
                routeUploadRequest(entityForUpload, forceEvenIfPriorUploaded, e);
            }
        });
    }

    private void routeUploadRequest(final EntityForUpload entity, boolean forceReUpload, ObservableEmitter<UploadResult> emitter) {
        if (entity.isUploading()) {
            emitter.onNext(new UploadResult(entity.getType(), new EntityAlreadyUploadingException()));
        } else if (!forceReUpload && entity.isUploaded()) {
            emitter.onNext(new UploadResult(entity.getType(), entity)); // maybe use a quasi-exception
        } else if (entity.priorEntitiesToUpload() != null && !entity.priorEntitiesToUpload().isEmpty()) {
            clearPriorUploadsNeeded(entity.priorEntitiesToUpload(), emitter);
        } else {
            UploadableEntityType type = entity.getType();
            switch (type) {
                case LIVEWIRE_ALERT:
                    uploadLiveWireAlert((LiveWireAlert) entity, emitter);
                    break;
                case MEDIA_FILE:
                    uploadMediaFile((MediaFile) entity, emitter);
                    break;
                default:
                    emitter.onNext(new UploadResult(type, new IllegalArgumentException("Unsupported type for uploading to retrieve UID")));
            }
        }
    }

    private void clearPriorUploadsNeeded(@NonNull List<EntityForUpload> priorQueue, ObservableEmitter<UploadResult> emitter) {
        for (int i = 0; i < priorQueue.size(); i++) {
            routeUploadRequest(priorQueue.get(i), false, emitter);
        }
    }

    private void uploadLiveWireAlert(final LiveWireAlert alert, ObservableEmitter<UploadResult> emitter) {
        executeCallWithUidResponse(alert, emitter, grassrootUserApi.createLiveWireAlert(
                currentUserUid,
                alert.getHeadline(),
                alert.getDescription(),
                alert.getAlertType(),
                alert.getGroupUid(),
                alert.getTaskUid(),
                false,
                0,
                0,
                null));
    }

    private void uploadMediaFile(final MediaFile mediaFile, ObservableEmitter<UploadResult> emitter) {
        executeCallWithUidResponse(mediaFile, emitter, grassrootUserApi.sendMediaFile(
                currentUserUid,
                mediaFile.getUid(),
                mediaFile.getLocalPath(),
                getImageFromPath(mediaFile, "file")
        ));
    }

    private void executeCallWithUidResponse(final EntityForUpload entity,
                                            ObservableEmitter<UploadResult> emitter,
                                            final Call<RestResponse<String>> networkCall) {
        try {
            Response<RestResponse<String>> response = networkCall.execute();
            if (response.isSuccessful()) {
                emitter.onNext(new UploadResult(entity.getType(), entity.getUid(), response.body().getData()));
            } else {
                emitter.onNext(new UploadResult(entity.getType(), new ServerErrorException()));
            }
        } catch (IOException error) {
            emitter.onNext(new UploadResult(UploadableEntityType.LIVEWIRE_ALERT, new NetworkUnavailableException()));
        }

    }

    public MultipartBody.Part getImageFromPath(final MediaFile mediaFile, final String paramName) {
        try {
            final File file = new File(mediaFile.getLocalPath());
            Timber.d("file size : " + (file.length() / 1024));
            RequestBody requestFile = RequestBody.create(MediaType.parse(mediaFile.getMimeType()), file);
            return MultipartBody.Part.createFormData(paramName, file.getName(), requestFile);
        } catch (Exception e) {
            return null;
        }
    }
}
