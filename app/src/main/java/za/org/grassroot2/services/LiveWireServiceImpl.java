package za.org.grassroot2.services;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
import za.org.grassroot2.BuildConfig;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.UploadResult;
import za.org.grassroot2.model.exception.LiveWireAlertNotCompleteException;

/**
 * Created by luke on 2017/08/15.
 */

public class LiveWireServiceImpl implements LiveWireService {

    private final DatabaseService databaseService;
    private final NetworkService networkService;

    @Inject
    public LiveWireServiceImpl(DatabaseService realmService, NetworkService networkService) {
        this.databaseService = realmService;
        this.networkService = networkService;
    }

    @Override
    public Maybe<LiveWireAlert> load(final String alertUid) {
        return databaseService.load(LiveWireAlert.class, alertUid);
    }

    @Override
    public Single<String> initiateAlertWithMedia(final String mediaFileUid) {
        return databaseService.load(MediaFile.class, mediaFileUid)
                .filter(mediaFile -> {
                    if (mediaFile == null) {
                        Timber.e("error! tried to load media file that is null, need recovery mechanism");
                        return false;
                    } else {
                        return true;
                    }
                })
                .flatMapSingle(mediaFile -> databaseService.store(LiveWireAlert.class, LiveWireAlert.newBuilder()
                        .mediaFile(mediaFile)
                        .build()))
                .map(liveWireAlert -> liveWireAlert == null ? null : liveWireAlert.getUid().toString());
    }

    @Override
    public Single<String> initiateAlertWithHeadline(final String headline) {
        return databaseService.store(LiveWireAlert.class, LiveWireAlert.newBuilder()
                .headline(headline)
                .build())
                .map(liveWireAlert -> liveWireAlert.getUid());
    }

    @Override
    public Single<String> initiateAlertWithGroupUid(final String groupUid) {
        return databaseService.store(LiveWireAlert.class, LiveWireAlert.newBuilder()
                .groupUid(groupUid)
                .build())
                .map(liveWireAlert -> liveWireAlert.getUid().toString());
    }

    @Override
    public Single<String> updateAlertMedia(final String alertUid, final String mediaFileUid) {
        Timber.e("associating an alert to media file");
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                final MediaFile mediaFile = databaseService.loadObjectByUid(MediaFile.class, mediaFileUid);
                Timber.e("mediaFile on DB load? " + mediaFile + ", for UID = " + mediaFileUid);
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setMediaFile(mediaFile);
                databaseService.storeObject(LiveWireAlert.class, alert);
                Timber.e("okay, we have set the media file");
                Timber.e("returning, with alert Uid = " + alertUid);
                Timber.e("uh, media file now? " + databaseService.loadObjectByUid(LiveWireAlert.class, alertUid).getMediaFile());
                e.onSuccess(alertUid);
            }
        });
    }

    @Override
    public Single<String> updateAlertHeadline(final String alertUid, final String headline) {
        return Single.create(e -> {
            final LiveWireAlert alert = loadAlertToEdit(alertUid);
            alert.setHeadline(headline);
            databaseService.storeObject(LiveWireAlert.class, alert);
            e.onSuccess(alertUid);
        });
    }

    @Override
    public Single<Boolean> updateAlertDescription(final String alertUid, final String description) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                Timber.e("looking for alert of UID = " + alertUid + ", to set description = " + description);
                final LiveWireAlert alert = databaseService.loadObjectByUid(LiveWireAlert.class, alertUid);
                alert.setDescription(description);
                databaseService.storeObject(LiveWireAlert.class, alert);
                Timber.e("alert description = " + databaseService.loadObjectByUid(LiveWireAlert.class, alertUid).getDescription());
                e.onSuccess(true);
            }
        });
    }

    @Override
    public Single<Boolean> setGenericAlert(final String alertUid, final String groupUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setAlertType(LiveWireAlert.TYPE_GENERIC);
                alert.setGroupUid(groupUid);
                databaseService.storeObject(LiveWireAlert.class, alert);
                e.onSuccess(true);
            }
        });
    }

    @Override
    public Single<Boolean> setMeetingAlert(final String alertUid, final String meetingUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setAlertType(LiveWireAlert.TYPE_MEETING);
                alert.setTaskUid(meetingUid);
                databaseService.storeObject(LiveWireAlert.class, alert);
            }
        });
    }

    @Override
    public Single<Boolean> markAlertReadyForDispatch(final String alertUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                if (alert.areMinimumFieldsComplete()) {
                    alert.setComplete(true);
                    databaseService.storeObject(LiveWireAlert.class, alert);
                    e.onSuccess(true);
                } else {
                    Timber.e("alert is not complete, entity = " + alert);
                    e.onError(new LiveWireAlertNotCompleteException());
                }
            }
        });
    }

    @Override
    public Observable<Boolean> triggerAlertDispatch(String alertUid) {
        LiveWireAlert alert = databaseService.loadObjectByUid(LiveWireAlert.class, alertUid);
        return networkService.uploadEntity(alert, false)
                .map(new Function<UploadResult, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull UploadResult uploadResult) throws Exception {
                        // and process it properly here
                        Timber.d("upload result: " + uploadResult);
                        // todo : store server UID();
                        return true;
                    }
                });
    }

    private LiveWireAlert loadAlertToEdit(final String alertUid) {
        if (BuildConfig.DEBUG) {
            databaseService.listAllEntitesOfType(LiveWireAlert.class);
        }
        Timber.e("looking for alert of UID : " + alertUid);
        return databaseService.loadObjectByUid(LiveWireAlert.class, alertUid);
    }

    private void tidyUp(SingleEmitter<Boolean> e) {
        e.onSuccess(true);
    }

    private void tidyUp(String uid, SingleEmitter<String> e) {
        e.onSuccess(uid);
    }

}
