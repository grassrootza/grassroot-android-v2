package za.org.grassroot2.presenter;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.LiveWireAlert;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnParameters;
import za.org.grassroot2.model.dto.BtnReturnBundle;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.util.StringDescriptionProvider;
import za.org.grassroot2.view.MainView;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

/**
 * Created by luke on 2017/08/11.
 * todo : reorder bunch of methods so reads logically
 */

public class MainPresenter extends LoggedInViewPresenterImpl<MainView> {

    private static final int TAKE_PHOTO_ACTION = 101;
    private static final int TAKE_AUDIO_ACTION = 102;
    private static final int TAKE_VIDEO_ACTION = 103;
    private static final int PICK_GALLERY_ACTION = 104;

    private static final int CONFIRM_ACTION = 201;
    private static final int MODIFY_ACTION = 202;
    private static final int CANCEL_ACTION = 203;

    private static final int STORED_FAILURE = 300;
    private static final int STORED_WITHOUT_UPLOAD = 301;
    private static final int STORED_UPLOAD_FAILED = 302;
    private static final int STORED_UPLOAD_SUCCEEDED = 303;

    private final DatabaseService databaseService;
    private final MediaService mediaService;
    private final LiveWireService liveWireService;
    private final StringDescriptionProvider descProvider;

    private String currentMediaFileUid; // during launching of intent
    private String currentLiveWireAlertUid;
    private boolean skippedMediaFile = false; // probably there's a more elegant way
    private boolean skippedDescription = false;

    @Inject
    public MainPresenter(StringDescriptionProvider provider, UserDetailsService userDetailsService,
                         DatabaseService realmService,
                         MediaService mediaService,
                         LiveWireService liveWireService) {
        super(userDetailsService);
        descProvider = provider;
        this.databaseService = realmService;
        this.mediaService = mediaService;
        this.liveWireService = liveWireService;
    }

    private Consumer<BtnReturnBundle> defaultScreenConsumer() {
        return btnReturnBundle -> {
            Timber.e("btnReturnBundle: " + btnReturnBundle);
                handleMediaButtonClick(btnReturnBundle);
        };
    }

    private void decideOnNextLiveWireStep() {
        view.closeProgressBar();
        disposableOnDetach(liveWireService.load(currentLiveWireAlertUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(liveWireAlert -> {
                    Timber.i("deciding next step, alert: " + liveWireAlert);
                    if (descProvider.isEmpty(liveWireAlert.getHeadline())) {
                        view.askForHeadline();
                    } else if (!skippedMediaFile && liveWireAlert.getMediaFile() == null) {
                        view.askForMediaFile();
                    } else if (descProvider.isEmpty(liveWireAlert.getAlertType())) {
                        // for now, default to group
                        view.loadGroupSelection();
                    } else if (!skippedDescription && descProvider.isEmpty(liveWireAlert.getDescription())) {
                        view.askForDescription();
                    } else {
                        view.askForConfirmation();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    showErrorAndReturnToNull();
                }));
    }

    private void showErrorAndReturnToNull() {
        view.showErrorToast(R.string.error_lwire_alert_not_found);
        currentLiveWireAlertUid = null;
        currentMediaFileUid = null;
    }

    public void createOrUpdateLiveWireAlertWithHeadline(String headline) {
        Timber.d("current alert UID = " + currentLiveWireAlertUid);
        Single<String> serviceCall = descProvider.isEmpty(currentLiveWireAlertUid) ?
                liveWireService.initiateAlertWithHeadline(String.valueOf(headline)) :
                liveWireService.updateAlertHeadline(currentLiveWireAlertUid, headline);

        disposableOnDetach(serviceCall.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Timber.e("initiated or updated a LiveWire alert, with UID = " + s);
                    currentLiveWireAlertUid = s;
                    decideOnNextLiveWireStep();
                }, Throwable::printStackTrace));
    }

    @Override
    public void handleActivityResult(int requestCode, Intent data) {
        if (requestCode == TAKE_PHOTO_ACTION) {
            view.showProgressBar();
            disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid, true)
                    .doOnError(this::handleMediaError)
                    .flatMap(s -> createOrUpdateAlertWithMedia(currentMediaFileUid))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> decideOnNextLiveWireStep(), this::handleMediaError));
        } else if (requestCode == PICK_GALLERY_ACTION) {
            disposableOnDetach(mediaService.storeGalleryFile(currentMediaFileUid, data.getData(), true)
                    .doOnError(this::handleMediaError)
                    .flatMap(s -> createOrUpdateAlertWithMedia(currentMediaFileUid))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> decideOnNextLiveWireStep(), this::handleMediaError));
        }
    }

    private Single<String> createOrUpdateAlertWithMedia(String mediaFileUid) {
        return descProvider.isEmpty(currentLiveWireAlertUid) ?
                liveWireService.initiateAlertWithMedia(mediaFileUid) :
                liveWireService.updateAlertMedia(currentLiveWireAlertUid, mediaFileUid);
    }

    // todo : improve/fix
    @Override
    public void handleActivityResultError(int requestCode, int resultCode, Intent data) {
        handleMediaError(new IllegalArgumentException());
    }

    private void handleMediaError(Throwable throwable) {
        view.closeProgressBar();
        Timber.e(throwable);
        view.showErrorToast(R.string.error_lwire_alert_media_error);
        decideOnNextLiveWireStep();
    }

    public void setGroupForAlert(String groupUid) {
        disposableOnDetach(liveWireService.setGenericAlert(currentLiveWireAlertUid, groupUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Timber.i("done! alert updated, now confirm / ask for video ...");
                    if (aBoolean) {
                        view.askForDescription();
                    } else {
                        Timber.e("oh dear, something went wrong");
                    }
                }, Throwable::printStackTrace));
    }

    public void setDescription(String description) {
        if (descProvider.isEmpty(description)) {
            skippedDescription = true;
            decideOnNextLiveWireStep();
        } else {
            disposableOnDetach(liveWireService.updateAlertDescription(currentLiveWireAlertUid, description)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> {
                        Timber.i("back in main, description? alert : " +
                                databaseService.loadObjectByUid(LiveWireAlert.class, currentLiveWireAlertUid));
                        disposableOnDetach(databaseService.load(LiveWireAlert.class, currentLiveWireAlertUid)
                                .subscribeOn(Schedulers.io())
                                .subscribe(liveWireAlert -> Timber.e("description? " + liveWireAlert.getDescription()), Throwable::printStackTrace));
                        decideOnNextLiveWireStep();
                    }, Throwable::printStackTrace));
        }
    }

    public String alertConfirmBody() {
        LiveWireAlert alert = databaseService.loadObjectByUid(LiveWireAlert.class, currentLiveWireAlertUid);
        String description;
        Timber.e("alert group UID: " + alert.getGroupUid());
        final String groupName = databaseService.loadGroup(alert.getGroupUid()).getName();
        if (alert.hasMedia()) {
            description = descProvider.getLivewireConfirmTextMedia(alert.getHeadline(), groupName, alert.getMediaFile().getMimeType());
        } else {
            description = descProvider.getLivewireConfirmNoMedia(alert.getHeadline(), groupName);
        }
        return description;
    }

    private void handleConfirmResponse(BtnReturnBundle btnReturnBundle) {
        // todo : handle cancel & modify, and option for upload later
        if (btnReturnBundle.getButtonActionCode() == CONFIRM_ACTION) {
            markAlertComplete(true);
        } else {
            Timber.e("Button return: " + btnReturnBundle);
        }
    }

    private void markAlertComplete(final boolean uploadNow) {
        disposableOnDetach(liveWireService.markAlertReadyForDispatch(currentLiveWireAlertUid)
                .subscribeOn(Schedulers.io())
                .subscribe(aBoolean -> {
                    if (uploadNow) {
                        triggerAlertUpload(currentLiveWireAlertUid);
                    } else {
                        handleStorageUploadResult(STORED_WITHOUT_UPLOAD);
                    }
                }, throwable -> {
                    Timber.e(throwable, "Error completing!");
                    handleStorageUploadResult(STORED_FAILURE);
                }));

    }

    // todo : build picker and sender in background to also handle this
    private void triggerAlertUpload(final String alertUid) {
        disposableOnDetach(liveWireService.triggerAlertDispatch(alertUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> handleStorageUploadResult(aBoolean ? STORED_UPLOAD_SUCCEEDED : STORED_UPLOAD_FAILED), throwable -> {
                    Timber.e(throwable, "Upload failed");
                    handleStorageUploadResult(STORED_UPLOAD_FAILED);
                }));
    }

    private void handleStorageUploadResult(int result) {
        if (result == STORED_UPLOAD_SUCCEEDED) {
            view.showSuccessMsg(R.string.done_header);
            view.goToDefaultScreen();
        }
    }

    public BtnGrouping getConfirmButtons() {
        BtnParameters confirm = BtnParameters.builder()
                .name("CONFIRM")
                .actionCode(CONFIRM_ACTION)
                .labelRes(R.string.button_confirm)
                .build();

        BtnParameters cancel = BtnParameters.builder()
                .name("CANCEL")
                .actionCode(CANCEL_ACTION)
                .labelRes(android.R.string.cancel)
                .build();

        BtnParameters modify = BtnParameters.builder()
                .name("MODIFY")
                .actionCode(MODIFY_ACTION)
                .labelRes(R.string.button_modify)
                .build();

        return BtnGrouping.builder()
                .firstSubMenu(new BtnParameters[] { cancel, modify, confirm })
                .build();
    }

    public void menuReady() {
        disposableOnDetach(view.logoutClicked().subscribe(valid -> {
            if (valid) {
                logoutWipingData();
            }
        }, Throwable::printStackTrace));

        disposableOnDetach(view.syncTriggered().subscribe(valid -> {
            if (valid) {
                Timber.i("sync triggered in main presenter");
                triggerAccountSync();
            }
        }, Throwable::printStackTrace));
    }

    public BtnGrouping obtainMediaButtons() {
        BtnParameters takePhoto = BtnParameters.builder()
                .name("TAKE_PHOTO")
                .actionCode(TAKE_PHOTO_ACTION)
                .drawableRes(R.mipmap.app_icon)
                .labelRes(R.string.take_photo)
                .build();

        BtnParameters takeVideo = BtnParameters.builder()
                .name("TAKE_VIDEO")
                .actionCode(TAKE_VIDEO_ACTION)
                .drawableRes(R.mipmap.app_icon)
                .labelRes(R.string.take_video)
                .build();

        BtnParameters takeAudio = BtnParameters.builder()
                .name("PICK_GALLERY")
                .actionCode(PICK_GALLERY_ACTION)
                .drawableRes(R.mipmap.app_icon)
                .labelRes(R.string.pick_gallery)
                .build();

        return BtnGrouping.builder()
                .firstSubMenu(new BtnParameters[] { takeVideo, takePhoto, takeAudio })
                .build();
    }

    public void handleMediaButtonClick(BtnReturnBundle parameters) {
        if (parameters != null) {
            switch (parameters.getButtonActionCode()) {
                case TAKE_PHOTO_ACTION:
                    tryLaunchPhoto();
                    break;
                case TAKE_AUDIO_ACTION:
                    tryLaunchAudio();
                    break;
                case TAKE_VIDEO_ACTION:
                    tryLaunchVideo();
                    break;
                case PICK_GALLERY_ACTION:
                    pickGallery();
                    break;
                case CONFIRM_ACTION:
                    handleConfirmResponse(parameters);
                    break;
                default:
                    Timber.e("ERROR! Tried to call button handlers without well defined action");
            }
        }
    }

    private void tryLaunchPhoto() {
        disposableOnDetach(view.ensureExternalStoragePermission().subscribe(granted -> {
            if (granted) {
                takePhoto();
            }
        }, Throwable::printStackTrace));
    }

    private void takePhoto() {
        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    MediaFile mediaFile = databaseService.loadObjectByUid(MediaFile.class, s);
                    Timber.e("mediaFile stored and retrieved, = " + mediaFile);
                    // for some reason, sometimes it comes back null ...
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(mediaFile.getContentProviderPath()));
                    cameraIntent.putExtra("MY_UID", s);
                    Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.getContentProviderPath()));
                    currentMediaFileUid = s;
                    view.launchActivityForResult(cameraIntent, TAKE_PHOTO_ACTION);
                }, throwable -> {
                    Timber.e(throwable, "Error creating file");
                    view.showErrorToast(R.string.error_file_creation);
                }));
    }

    private void pickGallery() {
        disposableOnDetach(mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                    intent.setType("image/*"); // for now
                    currentMediaFileUid = s;
                    view.launchActivityForResult(intent, PICK_GALLERY_ACTION);
                }, Throwable::printStackTrace));
    }

    private void tryLaunchVideo() {
        disposableOnDetach(view.ensureExternalStoragePermission().subscribe(aBoolean -> takeVideo(), Throwable::printStackTrace));
    }

    private void takeVideo() {
        /* try {
            Uri outputFileVideo = mediaService.createFileForMedia(".mpeg", MediaFile.FUNCTION_LIVEWIRE);
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileVideo);
            view.launchActivityForResult(videoIntent, TAKE_VIDEO_ACTION);
        } catch (IOException e) {
            Timber.e(e, "Error creating file");
            view.showErrorToast(R.string.error_file_creation);
        }*/
    }

    public void tryLaunchAudio() {
        disposableOnDetach(view.ensureAudioRecordingPermissions().subscribe(result -> {

        }, Throwable::printStackTrace));
    }

    public void skipMedia() {
        skippedMediaFile = true;
        decideOnNextLiveWireStep();
    }
}
