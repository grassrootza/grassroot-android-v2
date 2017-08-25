package za.org.grassroot.android.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;
import za.org.grassroot.android.model.Group;
import za.org.grassroot.android.model.LiveWireAlert;
import za.org.grassroot.android.model.MediaFile;
import za.org.grassroot.android.model.UploadResult;
import za.org.grassroot.android.model.enums.NetworkEntityType;
import za.org.grassroot.android.model.exception.EntityAlreadyUploadingException;
import za.org.grassroot.android.model.exception.NetworkUnavailableException;
import za.org.grassroot.android.model.exception.ServerErrorException;
import za.org.grassroot.android.model.network.EntityForDownload;
import za.org.grassroot.android.model.network.EntityForUpload;
import za.org.grassroot.android.services.rest.GrassrootUserApi;
import za.org.grassroot.android.services.rest.RestResponse;

/**
 * Created by luke on 2017/08/16.
 */

public class NetworkServiceImpl implements NetworkService {

    private final UserDetailsService userDetailsService;
    private final GrassrootUserApi grassrootUserApi;
    private final RealmService realmService;

    private String currentUserUid; // given frequency of calling/using, best to stash

    @Inject
    public NetworkServiceImpl(UserDetailsService userDetailsService,
                              GrassrootUserApi grassrootUserApi,
                              RealmService realmService) {
        this.userDetailsService = userDetailsService;
        this.grassrootUserApi = grassrootUserApi;
        this.realmService = realmService;
    }

    private void setUserUid() {
        if (currentUserUid == null) {
            currentUserUid = userDetailsService.getCurrentUserUid();
        }
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

    // as with below (upload method), know there must be a more RX 'pure' pattern to do this than passing
    // along the emitter, but I'm struggling to work out what, and defaulting to get work -> get clean
    @SuppressWarnings("unchecked")
    @Override
    public <E extends EntityForDownload> Observable<List<E>> downloadAllChangedOrNewEntities(final NetworkEntityType entityType, boolean forceFullRefresh) {
        setUserUid();
        Timber.e("user UID = ? " + currentUserUid);
        switch (entityType) {
            case GROUP:
                return downloadAllChangedOrNewGroups()
                        .flatMap(new Function<List<Group>, Observable<List<E>>>() {
                            @Override
                            public Observable<List<E>> apply(@NonNull List<Group> groups) throws Exception {
                                return Observable.just((List<E>) groups);
                            }
                        });
            default:
                throw new IllegalArgumentException("Error! Trying to download an unimplemented type");
        }
    }

    @Override
    public Observable<List<Group>> downloadAllChangedOrNewGroups() {
        return grassrootUserApi
                .fetchUserGroups(currentUserUid, realmService.loadExistingObjectsWithLastChangeTime(Group.class))
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable);
                    }
                })
                .filter(new Predicate<RestResponse<List<Group>>>() {
                    @Override
                    public boolean test(@NonNull RestResponse<List<Group>> listRestResponse) throws Exception {
                        Timber.e("filtering if group list empty");
                        return listRestResponse.getData() != null && !listRestResponse.getData().isEmpty();
                    }
                })
                .concatMap(new Function<RestResponse<List<Group>>, Observable<RestResponse<List<Group>>>>() {
                    @Override
                    public Observable<RestResponse<List<Group>>> apply(@NonNull RestResponse<List<Group>> listRestResponse) throws Exception {
                        Timber.e("getting group info for remainder");
                        List<Group> changedGroups = listRestResponse.getData();
                        List<String> changedUids = new ArrayList<>();
                        for (int i = 0; i < changedGroups.size(); i++) {
                            changedUids.add(changedGroups.get(i).getUid());
                        }
                        return grassrootUserApi.fetchGroupsInfo(currentUserUid, changedUids);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable);
                    }
                })
                .flatMap(new Function<RestResponse<List<Group>>, Observable<List<Group>>>() {
                    @Override
                    public Observable<List<Group>> apply(@NonNull RestResponse<List<Group>> listRestResponse) throws Exception {
                        Timber.d("alright, here are the full groups back: " + listRestResponse.getData());
                        return Observable.just(listRestResponse.getData());
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
            NetworkEntityType type = entity.getType();
            switch (type) {
                case LIVEWIRE_ALERT:
                    uploadLiveWireAlert((LiveWireAlert) entity, emitter);
                    break;
                case MEDIA_FILE:
                    Timber.i("having routed the upload request, calling for media file");
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
                mediaFile.getMediaFunction(),
                mediaFile.getMimeType(),
                getImageFromPath(mediaFile, "file")
        ));
    }

    private void executeCallWithUidResponse(final EntityForUpload entity,
                                            ObservableEmitter<UploadResult> emitter,
                                            final Call<RestResponse<String>> networkCall) {
        try {
            Response<RestResponse<String>> response = networkCall.execute();
            Timber.i("executed network call");
            if (response.isSuccessful()) {

                emitter.onNext(new UploadResult(entity.getType(), entity.getUid(), response.body().getData()));
            } else {
                Timber.e("error with upload! : {}", response.errorBody());
                emitter.onNext(new UploadResult(entity.getType(), new ServerErrorException()));
            }
        } catch (IOException error) {
            Timber.e(error, "IO error!");
            emitter.onNext(new UploadResult(NetworkEntityType.LIVEWIRE_ALERT, new NetworkUnavailableException()));
        }

    }

    private MultipartBody.Part getImageFromPath(final MediaFile mediaFile, final String paramName) {
        try {
            Timber.i("getting image from path : " + mediaFile.getAbsolutePath());
            final File file = new File(mediaFile.getAbsolutePath());
            Timber.d("file size : " + (file.length() / 1024));
            RequestBody requestFile = RequestBody.create(MediaType.parse(mediaFile.getMimeType()), file);
            return MultipartBody.Part.createFormData(paramName, file.getName(), requestFile);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }
}
