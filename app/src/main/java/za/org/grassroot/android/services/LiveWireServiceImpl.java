package za.org.grassroot.android.services;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;
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
    public Single<String> initiateAlertWithMedia(final String mediaFileUid) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                MediaFile mediaFile = realmService.loadObjectByUid(MediaFile.class, mediaFileUid, true);
                e.onSuccess(realmService.storeRealmObject(LiveWireAlert.newBuilder()
                        .mediaFile(mediaFile)
                        .build(), false).getUid());
                realmService.closeRealmOnThread();
            }
        });
    }

    @Override
    public Single<String> initiateAlertWithHeadline(final String headline) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                e.onSuccess(realmService.storeRealmObject(LiveWireAlert.newBuilder()
                        .headline(headline)
                        .build(), false).getUid());
                realmService.closeRealmOnThread();
            }
        });
    }

    @Override
    public Single<Boolean> updateAlertMedia(final String alertUid, final String mediaFileUid) {
        // note: Realm should take care of the updating, if don't close the thread
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                MediaFile mediaFile = realmService.loadObjectByUid(MediaFile.class, mediaFileUid, false);
                LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setMediaFile(mediaFile);
                tidyUp(e);
            }
        });
    }

    @Override
    public Single<String> updateAlertHeadline(final String alertUid, final String headline) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setHeadline(headline);
                tidyUp(alert.getUid(), e);
            }
        });
    }

    @Override
    public Single<Boolean> updateAlertDescription(final String alertUid, final String description) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, false);
                alert.setDescription(description);
                tidyUp(e);
            }
        });
    }

    @Override
    public Single<Boolean> setGenericAlert(final String alertUid, final String groupUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setAlertType(LiveWireAlert.TYPE_GENERIC);
                alert.setGroupUid(groupUid);
                tidyUp(e);
            }
        });
    }

    @Override
    public Single<Boolean> setMeetingAlert(final String alertUid, final String meetingUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                LiveWireAlert alert = loadAlertToEdit(alertUid);
                alert.setAlertType(LiveWireAlert.TYPE_MEETING);
                alert.setTaskUid(meetingUid);
                tidyUp(e);
            }
        });
    }

    @Override
    public Single<Boolean> markAlertReadyForDispatch(final String alertUid) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                LiveWireAlert alert = loadAlertToEdit(alertUid);
                if (alert.areMinimumFieldsComplete()) {
                    alert.setComplete(true);
                    tidyUp(e);
                } else {
                    e.onError(new LiveWireAlertNotCompleteException());
                    realmService.closeRealmOnThread();
                }
            }
        });
    }

    @Override
    public Observable<String> triggerAlertDispatch(String alertUid) {
        LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, true);
        return networkService.uploadEntity(alert, false)
                .map(new Function<UploadResult, String>() {
                    @Override
                    public String apply(@NonNull UploadResult uploadResult) throws Exception {
                        // and process it properly here
                        Timber.d("upload result: " + uploadResult);
                        return uploadResult.getServerUid();
                    }
                });
    }

    private LiveWireAlert loadAlertToEdit(final String alertUid) {
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
