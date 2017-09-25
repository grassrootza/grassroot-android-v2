package za.org.grassroot2.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.R;
import za.org.grassroot2.dagger.ApplicationContext;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.MediaFile;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnParameters;
import za.org.grassroot2.model.dto.BtnReturnBundle;
import za.org.grassroot2.model.exception.InvalidViewForPresenterException;
import za.org.grassroot2.services.LiveWireService;
import za.org.grassroot2.services.MediaService;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.view.MainView;
import za.org.grassroot2.view.fragment.TextInputFragment;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

/**
 * Created by luke on 2017/08/11.
 * todo : reorder bunch of methods so reads logically
 */

public class MainPresenterImpl extends LoggedInViewPresenterImpl implements MainPresenter  {


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

    public static int REQUEST_PERMISSION_CODE = 01;

    private final Context applicationContext;
    private final DatabaseService databaseService;
    private final MediaService mediaService;
    private final LiveWireService liveWireService;

    private MainView view;
    private String currentMediaFileUid; // during launching of intent
    private String currentLiveWireAlertUid;
    private boolean skippedMediaFile = false; // probably there's a more elegant way
    private boolean skippedDescription = false;

    @Inject
    public MainPresenterImpl(@ApplicationContext Context applicationContext,
                             UserDetailsService userDetailsService,
                             DatabaseService realmService,
                             MediaService mediaService,
                             LiveWireService liveWireService) {
        super(userDetailsService);
        this.applicationContext = applicationContext;
        this.databaseService = realmService;
        this.mediaService = mediaService;
        this.liveWireService = liveWireService;
    }

    @Override
    public void attach(MainView view) {
        try {
            super.attach(view);
            this.view = view;
        } catch (ClassCastException e) {
            handleGenericKnownException(new InvalidViewForPresenterException());
            super.detach(view);
        }
    }

    @Override
    public void onViewCreated() {
        Timber.e("inside on view created");
        setUpDefaultScreen(R.string.main_explanation, null, true);
    }

    private void setUpDefaultScreen(int headlineRes, Integer explanRes, boolean insideCreateCycle) {
        if (insideCreateCycle) {
            Timber.e("create cycle, starting up");
            subscriptions.add(view.mainTextNext()
                    .subscribe(defaultScreenConsumer()));
        } else {
            subscriptions.dispose();
            Timber.e("inside setting up default screen");
            subscriptions.add(view
                    .defaultRequestTextOrButtons(headlineRes, explanRes, false)
                    .subscribe(defaultScreenConsumer()));
        }
    }

    private Consumer<BtnReturnBundle> defaultScreenConsumer() {
        return new Consumer<BtnReturnBundle>() {
            @Override
            public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                Timber.e("btnReturnBundle: " + btnReturnBundle);
                if (btnReturnBundle.getButtonActionCode() == TextInputFragment.MAIN_TEXT_NEXT_ACTION) {
                    createOrUpdateLiveWireAlertWithHeadline("" + btnReturnBundle.getCharSequenceInTextInput());
                } else {
                    handleMediaButtonClick(btnReturnBundle);
                }
            }
        };
    }

    private void decideOnNextLiveWireStep() {
        view.closeProgressBar();
        // view.showSuccessMsg(R.string.done_header);
        liveWireService.load(currentLiveWireAlertUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(liveWireAlert -> {
                    Timber.i("deciding next step, alert: " + liveWireAlert);
                    if (TextUtils.isEmpty(liveWireAlert.getHeadline())) {
                        askForHeadline();
                    } else if (!skippedMediaFile && liveWireAlert.getMediaFile() == null) {
                        askForMediaFile();
                    } else if (TextUtils.isEmpty(liveWireAlert.getAlertType())) {
                        // for now, default to group
                        loadGroupSelection();
                    } else if (!skippedDescription && TextUtils.isEmpty(liveWireAlert.getDescription())) {
                        askForDescription();
                    } else {
                        askForConfirmation();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    showErrorAndReturnToNull();
                });
    }

    private void showErrorAndReturnToNull() {
        view.showErrorToast(R.string.error_lwire_alert_not_found);
        currentLiveWireAlertUid = null;
        currentMediaFileUid = null;
    }

    private void askForHeadline() {
        subscriptions.add(view.requestTextInputNextCancel(R.string.lwire_headline_title, R.string.lwire_headline_explan)
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(@NonNull CharSequence sequence) throws Exception {
                        createOrUpdateLiveWireAlertWithHeadline(String.valueOf(sequence));
                    }
                }));
    }

    private void createOrUpdateLiveWireAlertWithHeadline(String headline) {
        Timber.d("current alert UID = " + currentLiveWireAlertUid);
        Single<String> serviceCall = TextUtils.isEmpty(currentLiveWireAlertUid) ?
                liveWireService.initiateAlertWithHeadline(String.valueOf(headline)) :
                liveWireService.updateAlertHeadline(currentLiveWireAlertUid, headline);

        serviceCall.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        Timber.e("initiated or updated a LiveWire alert, with UID = " + s);
                        currentLiveWireAlertUid = s;
                        decideOnNextLiveWireStep();
                    }
                });
    }

    // todo : replace 'audio' with 'attach'
    private void askForMediaFile() {
        subscriptions.add(view.requestConfirmationOrAction(
                R.string.lwire_media_header,
                applicationContext.getString(R.string.lwire_media_prompt),
                obtainMediaButtons(), true)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        addMediaFileSubs();
                    }
                }));
    }

    private void addMediaFileSubs() {
        subscriptions.add(view.btnGroupClicked()
                .subscribe(new Consumer<BtnReturnBundle>() {
                    @Override
                    public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                        handleMediaButtonClick(btnReturnBundle);
                    }
                }));

        subscriptions.add(view.skipButtonClicked()
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        Timber.e("Skip button clicked!");
                        skippedMediaFile = true;
                        decideOnNextLiveWireStep();
                    }
                }));
    }

    // todo : check the string result return in deciding what next
    // todo : maybe start transition to next view even prior to turning off progress bar
    // todo : clean up redundancy
    @Override
    public void handleActivityResult(int requestCode, Intent data) {
        if (requestCode == TAKE_PHOTO_ACTION) {
            view.showProgressBar();
            mediaService.captureMediaFile(currentMediaFileUid, true)
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            handleMediaError(throwable);
                        }
                    })
                    .flatMap(new Function<String, SingleSource<? extends String>>() {
                        @Override
                        public SingleSource<? extends String> apply(@NonNull String s) throws Exception {
                            return createOrUpdateAlertWithMedia(currentMediaFileUid);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(@NonNull String s) throws Exception {
                            decideOnNextLiveWireStep();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            handleMediaError(throwable);
                        }
                    });
        } else if (requestCode == PICK_GALLERY_ACTION) {
            mediaService.storeGalleryFile(currentMediaFileUid, data.getData(), true)
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            handleMediaError(throwable);
                        }
                    })
                    .flatMap(new Function<String, SingleSource<? extends String>>() {
                        @Override
                        public SingleSource<? extends String> apply(@NonNull String s) throws Exception {
                            return createOrUpdateAlertWithMedia(currentMediaFileUid);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(@NonNull String s) throws Exception {
                            decideOnNextLiveWireStep();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            handleMediaError(throwable);
                        }
                    });
        }
    }

    private Single<String> createOrUpdateAlertWithMedia(String mediaFileUid) {
        return TextUtils.isEmpty(currentLiveWireAlertUid) ?
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

    private void loadGroupSelection() {
        Timber.i("loading group selection ... UID = " + currentLiveWireAlertUid);
        databaseService.listAllEntitesOfType(Group.class);
        subscriptions.add(view.requestSelection(
                R.string.group_select_title,
                databaseService.loadObjectsForSelection(Group.class))
                .subscribe(s -> {
                    Timber.i("selected! : " + s);
                    if (!TextUtils.isEmpty(currentLiveWireAlertUid)) {
                        setGroupForAlert(currentLiveWireAlertUid, s);
                    } else {
                        Timber.e("error! selection called without valid LiveWire alert in waiting");
                    }
                }, Throwable::printStackTrace));
    }

    private void setGroupForAlert(String alertUid, String groupUid) {
        liveWireService.setGenericAlert(alertUid, groupUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        Timber.i("done! alert updated, now confirm / ask for video ...");
                        if (aBoolean) {
                            // decideOnNextLiveWireStep();
                            askForDescription();
                        } else {
                            Timber.e("oh dear, something went wrong");
                        }
                    }
                });
    }

    private void askForDescription() {
        Timber.i("loading description request ...");
        subscriptions.add(view.requestLongDescriptionInput(R.string.lwire_description_header, R.string.lwire_description_hint)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        subscribeToDescription();
                    }
                }));
    }

    private void subscribeToDescription() {
        subscriptions.add(view.mainTextNext().subscribe(new Consumer<BtnReturnBundle>() {
            @Override
            public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                setDescription(String.valueOf(btnReturnBundle.getCharSequenceInTextInput()));
            }
        }));

        subscriptions.add(view.skipButtonClicked().subscribe(new Consumer<Object>() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                skippedDescription = true;
                decideOnNextLiveWireStep();
            }
        }));
    }

    // this was generating a lot of very weird weirdness, hence excessive logging for now
    private void setDescription(String description) {
        liveWireService.updateAlertDescription(currentLiveWireAlertUid, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        Timber.i("back in main, description? alert : " +
                                databaseService.loadObjectByUid(LiveWireAlert.class, currentLiveWireAlertUid));
                        databaseService.load(LiveWireAlert.class, currentLiveWireAlertUid)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Consumer<LiveWireAlert>() {
                                    @Override
                                    public void accept(@NonNull LiveWireAlert liveWireAlert) throws Exception {
                                        Timber.e("description? " + liveWireAlert.getDescription());
                                    }
                                });
                        decideOnNextLiveWireStep();
                    }
                });
    }

    private void askForConfirmation() {
        Timber.i("asking for confirmation ...");
        databaseService.load(LiveWireAlert.class, currentLiveWireAlertUid)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<LiveWireAlert>() {
                    @Override
                    public void accept(@NonNull LiveWireAlert liveWireAlert) throws Exception {
                        loadConfirmation(liveWireAlert);
                    }
                });
    }

    private void loadConfirmation(LiveWireAlert alert) {
        subscriptions.add(view.requestConfirmationOrAction(
                R.string.lwire_confirm_header,
                alertConfirmBody(alert),
                getConfirmButtons(), false).
                flatMap(new Function<Integer, Observable<BtnReturnBundle>>() {
                    @Override
                    public Observable<BtnReturnBundle> apply(@NonNull Integer integer) throws Exception {
                        return view.btnGroupClicked();
                    }
                })
                .subscribe(new Consumer<BtnReturnBundle>() {
                    @Override
                    public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                        handleConfirmResponse(btnReturnBundle);
                    }
                }));
    }

    private String alertConfirmBody(LiveWireAlert alert) {
        String description;
        Timber.e("alert group UID: " + alert.getGroupUid());
        final String groupName = databaseService.loadGroup(alert.getGroupUid()).getName();
        if (alert.hasMedia()) {
            description = applicationContext.getString(R.string.lwire_confirm_text_media,
                    alert.getHeadline(), groupName, alert.getMediaFile().getMimeType());
        } else {
            description = applicationContext.getString(R.string.lwire_confirm_text_no_media,
                    alert.getHeadline(), groupName);
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
        liveWireService.markAlertReadyForDispatch(currentLiveWireAlertUid)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (uploadNow) {
                            triggerAlertUpload(currentLiveWireAlertUid);
                        } else {
                            handleStorageUploadResult(STORED_WITHOUT_UPLOAD);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable, "Error completing!");
                        handleStorageUploadResult(STORED_FAILURE);
                    }
                });

    }

    // todo : build picker and sender in background to also handle this
    private void triggerAlertUpload(final String alertUid) {
        liveWireService.triggerAlertDispatch(alertUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        handleStorageUploadResult(aBoolean ? STORED_UPLOAD_SUCCEEDED : STORED_UPLOAD_FAILED);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable, "Upload failed");
                        handleStorageUploadResult(STORED_UPLOAD_FAILED);
                    }
                });
    }

    private void handleStorageUploadResult(int result) {
        if (result == STORED_UPLOAD_SUCCEEDED) {
            view.showSuccessMsg(R.string.done_header);
            setUpDefaultScreen(R.string.done_header, R.string.lwire_alert_sent, false);
        }
    }

    private BtnGrouping getConfirmButtons() {
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

    @Override
    public void menuReady() {
        subscriptions.add(view.logoutClicked().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean valid) throws Exception {
                if (valid) {
                    logoutWipingData();
                }
            }
        }));

        subscriptions.add(view.syncTriggered().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean valid) throws Exception {
                if (valid) {
                    Timber.i("sync triggered in main presenter");
                    triggerAccountSync();
                }
            }
        }));
    }

    @Override
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

    private void handleMediaButtonClick(BtnReturnBundle parameters) {
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
                default:
                    Timber.e("ERROR! Tried to call button handlers without well defined action");
            }
        }
    }

    private void tryLaunchPhoto() {
        if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
            takePhoto();
        } else {
            view.requestPermission(new String[] { WRITE_EXTERNAL_STORAGE });
        }
    }

    private void takePhoto() {
        mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        MediaFile mediaFile = databaseService.loadObjectByUid(MediaFile.class, s);
                        Timber.e("mediaFile stored and retrieved, = " + mediaFile);
                        // for some reason, sometimes it comes back null ...
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(mediaFile.getContentProviderPath()));
                        cameraIntent.putExtra("MY_UID", s);
                        Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.getContentProviderPath()));
                        currentMediaFileUid = s;
                        view.launchActivityForResult(cameraIntent, TAKE_PHOTO_ACTION);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable, "Error creating file");
                        view.showErrorToast(R.string.error_file_creation);
                    }
                });
    }

    private void pickGallery() {
        mediaService.createFileForMedia("image/jpeg", MediaFile.FUNCTION_LIVEWIRE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                        intent.setType("image/*"); // for now
                        currentMediaFileUid = s;
                        view.launchActivityForResult(intent, PICK_GALLERY_ACTION);
                    }
                });
    }

    private void tryLaunchVideo() {
        if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
            Timber.d("we have permission to record video");
            takeVideo();
        } else {
            view.requestPermission(new String[] {WRITE_EXTERNAL_STORAGE});
        }
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

    private void tryLaunchAudio() {
        if (checkPermissions(new String[] { WRITE_EXTERNAL_STORAGE, RECORD_AUDIO })) {
            //
            Timber.d("we have permission to record audio");

        } else {
            view.requestPermission(new String[] {WRITE_EXTERNAL_STORAGE, RECORD_AUDIO});
        }
    }

    private boolean checkPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissions(@NonNull String[] permissions) {
        boolean result = true;
        for (int i = 0; i < permissions.length; i++) {
            result &= ContextCompat.checkSelfPermission(applicationContext, permissions[i]) == PackageManager.PERMISSION_GRANTED;
        }
        return result;
    }

    @Override
    public void cleanUpForActivity() {
        Timber.i("Cleaning up activity, including closing Realm instance");
        userDetailsService.cleanUpForActivity();
    }
}
