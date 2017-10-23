package za.org.grassroot2.services;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import timber.log.Timber;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.RequestMapper;
import za.org.grassroot2.model.UploadResult;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.exception.EntityAlreadyUploadingException;
import za.org.grassroot2.model.exception.ServerErrorException;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.model.network.EntityForUpload;
import za.org.grassroot2.model.request.MemberRequestObject;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.rest.GrassrootUserApi;
import za.org.grassroot2.services.rest.RestResponse;

/**
 * Created by luke on 2017/08/16.
 */

public class NetworkServiceImpl implements NetworkService {

    private final UserDetailsService userDetailsService;
    private final GrassrootUserApi   grassrootUserApi;
    private final DatabaseService    databaseService;

    private String currentUserUid; // given frequency of calling/using, best to stash

    @Inject
    public NetworkServiceImpl(UserDetailsService userDetailsService,
                              GrassrootUserApi grassrootUserApi,
                              DatabaseService databaseService) {
        this.userDetailsService = userDetailsService;
        this.grassrootUserApi = grassrootUserApi;
        this.databaseService = databaseService;
        currentUserUid = userDetailsService.getCurrentUserUid();
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
    public <E extends EntityForDownload> Observable<List<E>> downloadAllChangedOrNewEntities(final GrassrootEntityType entityType, boolean forceFullRefresh) {
        Timber.e("user UID = ? " + currentUserUid);
        switch (entityType) {
            case GROUP:
                return downloadAllChangedOrNewGroups()
                        .flatMap(groups -> Observable.just((List<E>) groups));
            default:
                throw new IllegalArgumentException("Error! Trying to download an unimplemented type");
        }
    }

    @Override
    public Observable<List<Group>> downloadAllChangedOrNewGroups() {
        return grassrootUserApi
                .fetchUserGroups(currentUserUid, databaseService.loadExistingObjectsWithLastChangeTime(Group.class))
                .doOnError(Timber::e)
                .filter(listRestResponse -> {
                    Timber.e("filtering if group list empty, what does map look like? " + listRestResponse);
                    return listRestResponse != null && !listRestResponse.isEmpty();
                })
                .concatMap(groups -> {
                    Timber.e("getting group info for remainder");
                    List<String> changedUids = new ArrayList<>();
                    for (int i = 0; i < groups.size(); i++) {
                        changedUids.add(groups.get(i).getUid());
                    }
                    return grassrootUserApi.fetchGroupsInfo(currentUserUid, changedUids);
                })
                .doOnError(Timber::e)
                .flatMap(listRestResponse -> {
                    Timber.d("alright, here are the full groups back: " + listRestResponse);
                    return Observable.just(listRestResponse);
                });
    }

    @Override
    public Observable<Long> getTimestampForText(String date) {
        return grassrootUserApi.getTimestampForTextDate(date).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Task>> downloadTaskMinimumInfo() {
        return grassrootUserApi
                .fetchUserTasksMinimumInfo(currentUserUid, databaseService.getAllTasksLastChangedTimestamp())
                .doOnError(Timber::e);
    }

    @Override
    public Observable<Response<Void>> inviteContactsToGroup(String groupId, List<Contact> contacts) {
        List<MemberRequestObject> body = new ArrayList<>();
        for (Contact c : contacts) {
            body.add(RequestMapper.map(c));
        }
        return grassrootUserApi.addMembersToGroup(currentUserUid, groupId, body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Task>> getTasksForGroup(String groupId) {
        return grassrootUserApi.fetchGroupTasksMinimumInfo(currentUserUid, groupId, databaseService.getTasksLastChangedTimestamp(groupId)).flatMap(listRestResponse -> {
            if (listRestResponse != null) {
                Map<String, String> uids = new HashMap<>();
                for (Task t : listRestResponse) {
                    uids.put(t.getUid(), t.getType().name());
                }
                return grassrootUserApi.fetchTasksByUid(currentUserUid, uids);
            } else {
                return Observable.just(new ArrayList<>());
            }
        });
    }

    @Override
    public Observable<List<Task>> getTasksByUids(Map<String, String> uids) {
        return grassrootUserApi
                .fetchTasksByUid(currentUserUid, uids)
                .doOnError(Timber::e);
    }

    @Override
    public Flowable<Resource<Task>> createTask(Task t) {
        return Flowable.create(e -> new ResourceToStore<Task, Task>(t, e) {
            @Override
            public Observable<Task> uploadRemote(Task localObject) {
                Meeting m = (Meeting) localObject;
                return grassrootUserApi.createTask("GROUP", currentUserUid, t.getParentUid(), m.getName(), m.getLocationDescription(), t.getDeadlineMillis());
            }

            @Override
            public void uploadFailed(Task localObject) {
                //                databaseService.storeTasks(Collections.singletonList(localObject));
            }

            @Override
            public void saveResult(Task data) {
                databaseService.storeTasks(Collections.singletonList(data));
            }
        }, BackpressureStrategy.BUFFER);
//        if (t instanceof Meeting) {
//            Meeting m = (Meeting) t;
//            return grassrootUserApi.createTask("GROUP", currentUserUid, t.getParentUid(), m.getName(), m.getLocationDescription(), t.getDeadlineMillis());
//        }
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
                            .concatMap(uploadResult -> mainEntityUpload);
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
        return grassrootUserApi.createLiveWireAlert(
                currentUserUid,
                alert.getHeadline(),
                TextUtils.isEmpty(alert.getDescription()) ? "" : alert.getDescription(), // very temp hack to avoid a redeploy of main platform just to make required (remove in future)
                alert.getAlertType(),
                alert.getGroupUid(),
                alert.getTaskUid(),
                false,
                0,
                0,
                alert.getMediaFileKeys()).flatMap(successHandler(alert)).onErrorResumeNext(resumeHandler(alert)).concatMap(uploadResult -> {
            if (uploadResult.getServerUid() != null) {
                alert.setServerUid(uploadResult.getServerUid());
                alert.setUnderReview(true);
                databaseService.storeObject(LiveWireAlert.class, alert);
            }
            return Observable.just(uploadResult);
        });
    }

    @NonNull
    private Function<Response<RestResponse<String>>, ObservableSource<? extends UploadResult>> successHandler(EntityForUpload alert) {
        return restResponseResponse -> {
            if (restResponseResponse.isSuccessful()) {
                return Observable.just(new UploadResult(alert.getType(), alert.getUid(), restResponseResponse.body().getData()));
            } else {
                return Observable.just(new UploadResult(alert.getType(), new ServerErrorException()));
            }
        };
    }

    @NonNull
    private Function<Throwable, ObservableSource<? extends UploadResult>> resumeHandler(EntityForUpload alert) {
        return throwable -> {
            if (throwable instanceof IOException) {
                return Observable.just(new UploadResult(alert.getType(), new Throwable()));
            } else {
                return Observable.just(new UploadResult(alert.getType(), new IllegalArgumentException()));
            }
        };
    }

    private Observable<UploadResult> uploadMediaFile(final MediaFile mediaFile) {
        return grassrootUserApi.sendMediaFile(
                currentUserUid,
                mediaFile.getUid(),
                mediaFile.getMediaFunction(),
                mediaFile.getMimeType(),
                getImageFromPath(mediaFile, "file")).flatMap(successHandler(mediaFile)).onErrorResumeNext(resumeHandler(mediaFile)).concatMap(uploadResult -> {
            if (uploadResult.getServerUid() != null) {
                mediaFile.setSentUpstream(true);
                mediaFile.setServerUid(uploadResult.getServerUid());
                databaseService.storeObject(MediaFile.class, mediaFile);
            }
            return Observable.just(uploadResult);
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
