package za.org.grassroot.android.services;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.realm.Realm;
import timber.log.Timber;
import za.org.grassroot.android.BuildConfig;
import za.org.grassroot.android.model.LiveWireAlert;
import za.org.grassroot.android.model.MediaFile;
import za.org.grassroot.android.model.UploadResult;
import za.org.grassroot.android.model.exception.LiveWireAlertNotCompleteException;

/**
 * Created by luke on 2017/08/15.
 */

public class LiveWireServiceImpl implements LiveWireService {

    private final RealmService realmService;
    private final NetworkService networkService;

    @Inject
    public LiveWireServiceImpl(RealmService realmService, NetworkService networkService) {
        this.realmService = realmService;
        this.networkService = networkService;
    }

    @Override
    public Single<LiveWireAlert> load(final String alertUid) {
        return realmService.load(LiveWireAlert.class, alertUid);
    }

    @Override
    public Single<String> initiateAlertWithMedia(final String mediaFileUid) {
        return realmService.load(MediaFile.class, mediaFileUid)
                .filter(new Predicate<MediaFile>() {
                    @Override
                    public boolean test(@NonNull MediaFile mediaFile) throws Exception {
                        if (mediaFile == null) {
                            Timber.e("error! tried to load media file that is null, need recovery mechanism");
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                .flatMapSingle(new Function<MediaFile, Single<LiveWireAlert>>() {
                    @Override
                    public Single<LiveWireAlert> apply(@NonNull MediaFile mediaFile) throws Exception {
                        return realmService.store(LiveWireAlert.newBuilder()
                                .mediaFile(mediaFile)
                                .build());
                    }
                })
                .map(new Function<LiveWireAlert, String>() {
                    @Override
                    public String apply(@NonNull LiveWireAlert liveWireAlert) throws Exception {
                        return liveWireAlert == null ? null : liveWireAlert.getUid();
                    }
                });
    }

    @Override
    public Single<String> initiateAlertWithHeadline(final String headline) {
        return realmService.store(LiveWireAlert.newBuilder()
                .headline(headline)
                .build())
                .map(new Function<LiveWireAlert, String>() {
                    @Override
                    public String apply(@NonNull LiveWireAlert liveWireAlert) throws Exception {
                        return liveWireAlert.getUid();
                    }
                });
    }

    @Override
    public Single<String> updateAlertMedia(final String alertUid, final String mediaFileUid) {
        Timber.e("associating an alert to media file");
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                final MediaFile mediaFile = realmService.loadObjectByUid(MediaFile.class, mediaFileUid, false);
                Timber.e("mediaFile on DB load? " + mediaFile + ", for UID = " + mediaFileUid);
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                realmService.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        alert.setMediaFile(mediaFile);
                        realm.copyToRealmOrUpdate(alert);
                        Timber.e("okay, we have set the media file");
                    }
                });
                Timber.e("returning, with alert Uid = " + alertUid);
                Timber.e("uh, media file now? " + realmService.loadObjectByUid(LiveWireAlert.class, alertUid, true).getMediaFile());
                e.onSuccess(alertUid);
            }
        });
    }

    @Override
    public Single<String> updateAlertHeadline(final String alertUid, final String headline) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                final LiveWireAlert alert = loadAlertToEdit(alertUid);
                realmService.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        alert.setHeadline(headline);
                        realm.copyToRealmOrUpdate(alert);
                    }
                });
                e.onSuccess(alertUid);
            }
        });
    }

    @Override
    public Single<Boolean> updateAlertDescription(final String alertUid, final String description) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                Timber.e("looking for alert of UID = " + alertUid + ", to set description = " + description);
                final LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, false);
                realmService.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        alert.setDescription(description);
                        realm.copyToRealmOrUpdate(alert);
                    }
                });
                Timber.e("alert description = " + realmService.loadObjectByUid(LiveWireAlert.class, alertUid, true).getDescription());
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
                realmService.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        alert.setAlertType(LiveWireAlert.TYPE_GENERIC);
                        alert.setGroupUid(groupUid);
                        realm.copyToRealmOrUpdate(alert);
                    }
                });
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
                realmService.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        alert.setAlertType(LiveWireAlert.TYPE_MEETING);
                        alert.setTaskUid(meetingUid);
                        realm.copyToRealmOrUpdate(alert);
                    }
                });
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
                    realmService.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            alert.setComplete(true);
                        }
                    });
                    e.onSuccess(true);
                } else {
                    Timber.e("alert is not complete, entity = " + alert);
                    e.onError(new LiveWireAlertNotCompleteException());
                    realmService.closeRealmOnThread();
                }
            }
        });
    }

    @Override
    public Observable<Boolean> triggerAlertDispatch(String alertUid) {
        LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, true);
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
            realmService.listAllEntitesOfType(LiveWireAlert.class);
        }
        Timber.e("looking for alert of UID : " + alertUid);
        return realmService.loadObjectByUid(LiveWireAlert.class, alertUid, false);
    }

    private void tidyUp(SingleEmitter<Boolean> e) {
        e.onSuccess(true);
        realmService.closeRealmOnThread();
    }

    private void tidyUp(String uid, SingleEmitter<String> e) {
        e.onSuccess(uid);
        realmService.closeRealmOnThread();
    }

}
