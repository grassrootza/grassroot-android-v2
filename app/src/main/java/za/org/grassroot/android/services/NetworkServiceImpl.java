package za.org.grassroot.android.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.realm.Realm;
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
        currentUserUid = userDetailsService.getCurrentUserUid();
        return routeUpload(entityForUpload, forceEvenIfPriorUploaded);
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

    private Observable<UploadResult> routeUpload(final EntityForUpload entity, boolean forceUpload) {
        if (entity.isUploading()) {
            return Observable.just(new UploadResult(entity.getType(), new EntityAlreadyUploadingException()));
        } else if (!forceUpload && entity.isUploaded()) {
            return Observable.just(new UploadResult(entity.getType(), entity));
        } else {
            boolean hasPriorEntitiesToUpload = entity.priorEntitiesToUpload() != null && !entity.priorEntitiesToUpload().isEmpty();
            final Observable<UploadResult> mainEntityUpload;
            switch (entity.getType()) {
                case MEDIA_FILE:
                    mainEntityUpload = uploadMediaFile((MediaFile) entity);
                    break;
                case LIVEWIRE_ALERT:
                    mainEntityUpload = uploadLiveWireAlert((LiveWireAlert) entity);
                    break;
                default:
                    return Observable.just(new UploadResult(entity.getType(), new IllegalArgumentException("Unsupported type for uploading to retrieve UID")));
            }
            return !hasPriorEntitiesToUpload ? mainEntityUpload :
                    clearPriorUploads(entity.priorEntitiesToUpload())
                            .concatMap(new Function<UploadResult, ObservableSource<? extends UploadResult>>() {
                                @Override
                                public ObservableSource<? extends UploadResult> apply(@NonNull UploadResult uploadResult) throws Exception {
                                    return mainEntityUpload;
                                }
                            });
        }
    }

    // todo : be careful of exactly how merging is done in here, merge is probably not the right operator
    private Observable<UploadResult> clearPriorUploads(List<EntityForUpload> priorQueue) {
        List<Observable<UploadResult>> uploadResults = new ArrayList<>();
        for (int i = 0; i < priorQueue.size(); i++) {
            uploadResults.add(routeUpload(priorQueue.get(i), false));
        }
        return Observable.merge(uploadResults);
    }

    private Observable<UploadResult> uploadLiveWireAlert(final LiveWireAlert alert) {
        final Call<RestResponse<String>> call = grassrootUserApi.createLiveWireAlert(
                currentUserUid,
                alert.getHeadline(),
                alert.getDescription(),
                alert.getAlertType(),
                alert.getGroupUid(),
                alert.getTaskUid(),
                false,
                0,
                0,
                alert.getMediaFileKeys());
        return executeUploadForUid(alert, call)
                .concatMap(new Function<UploadResult, ObservableSource<? extends UploadResult>>() {
                    @Override
                    public ObservableSource<? extends UploadResult> apply(@NonNull final UploadResult uploadResult) throws Exception {
                        if (uploadResult.getServerUid() != null) {
                            realmService.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    alert.setServerUid(uploadResult.getServerUid());
                                    alert.setUnderReview(true);
                                    realm.copyToRealmOrUpdate(alert);
                                }
                            });
                        }
                        return Observable.just(uploadResult);
                    }
                });
    }

    private Observable<UploadResult> uploadMediaFile(final MediaFile mediaFile) {
        final Call<RestResponse<String>> call = grassrootUserApi.sendMediaFile(
                currentUserUid,
                mediaFile.getUid(),
                mediaFile.getMediaFunction(),
                mediaFile.getMimeType(),
                getImageFromPath(mediaFile, "file"));
        return executeUploadForUid(mediaFile, call)
                .concatMap(new Function<UploadResult, ObservableSource<? extends UploadResult>>() {
                    @Override
                    public ObservableSource<? extends UploadResult> apply(@NonNull final UploadResult uploadResult) throws Exception {
                        if (uploadResult.getServerUid() != null) {
                            realmService.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    mediaFile.setSentUpstream(true);
                                    mediaFile.setServerUid(uploadResult.getServerUid());
                                    realm.copyToRealmOrUpdate(mediaFile);
                                }
                            });
                        }
                        return Observable.just(uploadResult);
                    }
                });
    }

    private Observable<UploadResult> executeUploadForUid(final EntityForUpload entity, final Call<RestResponse<String>> networkCall) {
        return Observable.create(new ObservableOnSubscribe<UploadResult>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<UploadResult> e) throws Exception {
                try {
                    Response<RestResponse<String>> response = networkCall.execute();
                    if (response.isSuccessful()) {
                        e.onNext(new UploadResult(entity.getType(), entity.getUid(), response.body().getData()));
                    } else {
                        e.onNext(new UploadResult(entity.getType(), new ServerErrorException()));
                    }
                } catch (IOException t1) {
                    e.onNext(new UploadResult(entity.getType(), new NetworkUnavailableException()));
                } catch (NullPointerException t2) {
                    e.onNext(new UploadResult(entity.getType(), new IllegalArgumentException()));
                }
            }
        });
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
