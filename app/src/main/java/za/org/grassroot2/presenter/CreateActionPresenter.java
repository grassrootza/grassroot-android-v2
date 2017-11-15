package za.org.grassroot2.presenter;

import android.net.Uri;
import android.text.TextUtils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.GrassrootView;


public class CreateActionPresenter extends BasePresenter<CreateActionPresenter.CreateActionView> {

    public enum ActionType {
        Meeting, LivewireAlert;
    }

    private NetworkService networkService;

    private MediaService    mediaService;
    private DatabaseService dbService;
    private LiveWireService liveWireService;

    private Task          taskToCreate;
    private String        currentMediaFileUid;
    private LiveWireAlert liveWireAlert;

    @Inject
    public CreateActionPresenter(NetworkService networkService, DatabaseService dbService, LiveWireService liveWireService, MediaService mediaService) {
        this.networkService = networkService;
        this.dbService = dbService;
        this.liveWireService = liveWireService;
        this.mediaService = mediaService;
    }

    public void initTask(ActionType type) {
        switch (type) {
            case Meeting:
                taskToCreate = new Meeting();
                taskToCreate.setUid(UUID.randomUUID().toString());
                break;
            case LivewireAlert:
                liveWireAlert = new LiveWireAlert();
                break;
        }
    }

    public void createMeeting() {
        getView().showProgressBar();
        disposableOnDetach(networkService.createTask(taskToCreate).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(task -> {
            getView().closeProgressBar();
            getView().uploadSuccessfull(GrassrootEntityType.MEETING);
        }, throwable -> {
            getView().closeProgressBar();
            getView().uploadSuccessfull(GrassrootEntityType.MEETING);
        }));
    }

    public void createAlert() {
        if (liveWireAlert.areMinimumFieldsComplete()) {
            getView().showProgressBar();
            liveWireAlert.setComplete(true);
            if (currentMediaFileUid != null) {
                liveWireAlert.setMediaFile(dbService.loadObjectByUid(MediaFile.class, currentMediaFileUid));
            }
            disposableOnDetach(dbService.store(LiveWireAlert.class, liveWireAlert).flatMapObservable(liveWireAlert1 -> networkService.uploadEntity(liveWireAlert1, false)).flatMap(uploadResult -> {
                if (!TextUtils.isEmpty(uploadResult.getServerUid())) {
                    liveWireAlert.setServerUid(uploadResult.getServerUid());
                } else {
                    liveWireAlert.setSynced(false);
                }
                dbService.storeObject(LiveWireAlert.class, liveWireAlert);
                return Observable.just(uploadResult);
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(uploadResult -> {
                        getView().closeProgressBar();
                        getView().uploadSuccessfull(GrassrootEntityType.LIVE_WIRE_ALERT);
                    }, throwable -> {
                        getView().closeProgressBar();
                        getView().uploadSuccessfull(GrassrootEntityType.LIVE_WIRE_ALERT);
                    }));
        }
    }

//    private void handleStorageUploadResult(int result) {
//        if (result == STORED_UPLOAD_SUCCEEDED) {
//            view.showSuccessMsg(R.string.done_header);
//            view.goToDefaultScreen();
//        }
//    }

    public void verifyGroupPermissions(String groupUid) {
        if (groupUid != null) {
            dbService.load(Group.class, groupUid).subscribe(group -> getView().proceedWithRender(group), Throwable::printStackTrace);
        } else {
            getView().proceedWithRender(null);
        }
    }

    public void setMeetingDate(Long date) {
        ((Meeting) taskToCreate).setDeadlineMillis(date);
        ((Meeting) taskToCreate).setCreatedDate(System.currentTimeMillis());
    }

    public void setMeetingLocation(String location) {
        ((Meeting) taskToCreate).setLocationDescription(location);
    }

    public void setSubject(String subject) {
        ((Meeting) taskToCreate).setSubject(subject);
    }

    public void setGroupUid(Group group) {
        if (liveWireAlert != null) {
            liveWireAlert.setAlertType(LiveWireAlert.TYPE_GENERIC);
            liveWireAlert.setGroupUid(group.getUid());
        } else {
            taskToCreate.setParentUid(group.getUid());
        }
    }

    public Maybe<Map.Entry<String, LiveWireAlert>> getAlertAndGroupName() {
        return dbService.load(Group.class, liveWireAlert.getGroupUid()).map(group -> new AbstractMap.SimpleEntry<>(group.getName(), liveWireAlert));
    }

    public void takePhoto() {
        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    MediaFile mediaFile = dbService.loadObjectByUid(MediaFile.class, s);
                    Timber.e("mediaFile stored and retrieved, = " + mediaFile);
                    // for some reason, sometimes it comes back null ...
                    Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.getContentProviderPath()));
                    currentMediaFileUid = s;
                    getView().cameraForResult(mediaFile.getContentProviderPath(), s);
                }, throwable -> {
                    Timber.e(throwable, "Error creating file");
                    getView().showErrorSnackbar(R.string.error_file_creation);
                }));
    }

    public void pickFromGallery() {
        disposableOnDetach(getView().ensureWriteExteralStoragePermission().flatMapSingle(aBoolean -> {
            if (aBoolean) {
                return mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE);
            }
            throw new Exception("Permission not granted");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    currentMediaFileUid = s;
                    getView().pickFromGallery();
                }, Throwable::printStackTrace));
    }

    public void recordVideo() {
//        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    MediaFile mediaFile = dbService.loadObjectByUid(MediaFile.class, s);
//                    Timber.e("mediaFile stored and retrieved, = " + mediaFile);
//                    // for some reason, sometimes it comes back null ...
//                    Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.getContentProviderPath()));
//                    currentMediaFileUid = s;
//                    view.videoForResult(mediaFile.getContentProviderPath(), s);
//                }, throwable -> {
//                    Timber.e(throwable, "Error creating file");
//                    view.showErrorSnackbar(R.string.error_file_creation);
//                }));
    }

    public void setLongDescription(String description) {
        if (!TextUtils.isEmpty(description)) {
            liveWireAlert.setDescription(description);
        }
    }

    public Task getTask() {
        return taskToCreate;
    }

    public LiveWireAlert getAlert() {
        return liveWireAlert;
    }

    public void cameraResult() {
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid)
                .doOnError(this::handleMediaError)
                .subscribe(s -> {
                    liveWireAlert.setMediaFile(dbService.loadObjectByUid(MediaFile.class, currentMediaFileUid));
                }));
    }

    public void handlePickResult(Uri data) {
        disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid, data).subscribeOn(Schedulers.io())
                .doOnError(this::handleMediaError)
                .subscribe(s -> liveWireAlert.setMediaFile(dbService.loadObjectByUid(MediaFile.class, currentMediaFileUid)), this::handleMediaError));
    }

    public void setHeadline(String headline) {
        liveWireAlert.setHeadline(headline);
    }

    private void handleMediaError(Throwable throwable) {
        getView().closeProgressBar();
        Timber.e(throwable);
        getView().showErrorSnackbar(R.string.error_lwire_alert_media_error);
    }

    public interface CreateActionView extends GrassrootView {
        Observable<Boolean> ensureWriteExteralStoragePermission();

        void uploadSuccessfull(GrassrootEntityType meeting);

        void closeScreen();

        void proceedWithRender(Group group);

        void cameraForResult(String contentProviderPath, String s);

        void videoForResult(String contentProviderPath, String s);

        void pickFromGallery();
    }

}
