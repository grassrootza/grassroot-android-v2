package za.org.grassroot.android.services;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import za.org.grassroot.android.model.LiveWireAlert;
import za.org.grassroot.android.model.MediaFile;

/**
 * Created by luke on 2017/08/15.
 */

class LiveWireServiceImpl implements LiveWireService {

    private final RealmService realmService;

    @Inject
    public LiveWireServiceImpl(RealmService realmService) {
        this.realmService = realmService;
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
                LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, false);
                alert.setMediaFile(mediaFile);
                e.onSuccess(true);
            }
        });
    }

    @Override
    public Single<Boolean> updateAlertHeadline(final String alertUid, final String headline) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {
                LiveWireAlert alert = realmService.loadObjectByUid(LiveWireAlert.class, alertUid, false);
                alert.setHeadline(headline);
                e.onSuccess(true);
            }
        });
    }

    @Override
    public Single<Boolean> updateAlertDescription(String alertUid, String description) {
        return null;
    }

    @Override
    public Single<Boolean> updateAlertGroupUid(String alertUid, String groupUid) {
        return null;
    }

    @Override
    public Single<Boolean> updateAlertTaskUid(String alertUid, String taskUid, String taskType) {
        return null;
    }

    @Override
    public Single<Boolean> markAlertReadyForReview(String alertUid) {
        return null;
    }
}
