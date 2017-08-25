package za.org.grassroot.android.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.dagger.ApplicationContext;
import za.org.grassroot.android.model.Group;
import za.org.grassroot.android.model.LiveWireAlert;
import za.org.grassroot.android.model.MediaFile;
import za.org.grassroot.android.model.dto.BtnGrouping;
import za.org.grassroot.android.model.dto.BtnParameters;
import za.org.grassroot.android.model.dto.BtnReturnBundle;
import za.org.grassroot.android.model.exception.InvalidViewForPresenterException;
import za.org.grassroot.android.services.LiveWireService;
import za.org.grassroot.android.services.MediaService;
import za.org.grassroot.android.services.RealmService;
import za.org.grassroot.android.services.UserDetailsService;
import za.org.grassroot.android.view.MainView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by luke on 2017/08/11.
 * todo : reorder bunch of methods so reads logically
 */

public class MainPresenterImpl extends LoggedInViewPresenterImpl implements MainPresenter  {

    private static final int TAKE_PHOTO_ACTION = 101;
    private static final int TAKE_AUDIO_ACTION = 102;
    private static final int TAKE_VIDEO_ACTION = 103;

    private static final int CONFIRM_ACTION = 201;
    private static final int MODIFY_ACTION = 202;
    private static final int CANCEL_ACTION = 203;

    public static int REQUEST_PERMISSION_CODE = 01;

    private final Context applicationContext;
    private final RealmService realmService;
    private final MediaService mediaService;
    private final LiveWireService liveWireService;

    private MainView view;
    private String currentMediaFileUid; // during launching of intent

    private String currentLiveWireAlertUid;

    @Inject
    public MainPresenterImpl(@ApplicationContext Context applicationContext,
                             UserDetailsService userDetailsService,
                             RealmService realmService,
                             MediaService mediaService,
                             LiveWireService liveWireService) {
        super(userDetailsService);
        this.applicationContext = applicationContext;
        this.realmService = realmService;
        this.mediaService = mediaService;
        this.liveWireService = liveWireService;
    }

    @Override
    public void attach(MainView view) {
        try {
            super.attach(view);
            this.view = view;
            realmService.openUiRealm();
        } catch (ClassCastException e) {
            handleGenericKnownException(new InvalidViewForPresenterException());
            super.detach(view);
        }
    }

    @Override
    public void onViewCreated() {
        subscriptions.add(view.threeButtonRowButtonClicked()
                .subscribe(new Consumer<BtnReturnBundle>() {
            @Override
            public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                Timber.d("btnReturnBundle: " + btnReturnBundle);
                handleSubButtonClick(btnReturnBundle);
            }
        }));

        subscriptions.add(view.mainTextNext()
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(@NonNull CharSequence sequence) throws Exception {
                        createOrUpdateLiveWireAlertWithHeadline(String.valueOf(sequence));
                    }
                }));

    }

    private void decideOnNextLiveWireStep() {
        liveWireService.load(currentLiveWireAlertUid).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LiveWireAlert>() {
                    @Override
                    public void accept(@NonNull LiveWireAlert liveWireAlert) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                    }
                });
    }

    private void createOrUpdateLiveWireAlertWithHeadline(String headline) {
        Timber.e("current alert UID = " + currentLiveWireAlertUid);
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
                        // realmService.listAllEntitesOfType(LiveWireAlert.class);
                        // decideOnNextLiveWireStep();
                        loadGroupSelection();
                    }
                });
    }

    private void askForHeadline() {
        Timber.e("loading headline requestor ...");
        subscriptions.add(view.requestTextInputNextCancel(R.string.lwire_headline_title, R.string.lwire_headline_explan)
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(@NonNull CharSequence sequence) throws Exception {
                        createOrUpdateLiveWireAlertWithHeadline(String.valueOf(sequence));
                    }
                }));
    }

    private void askForDescription() {
        Timber.i("loading description request ...");
        subscriptions.add(view.requestLongDescriptionInput(R.string.lwire_description_header, R.string.lwire_description_hint)
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(@NonNull CharSequence sequence) throws Exception {
                        Timber.e("got a description! : " + sequence);
                        setDescription(String.valueOf(sequence));
                    }
                }));
    }

    private void setDescription(String description) {
        liveWireService.updateAlertDescription(currentLiveWireAlertUid, description)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        askForConfirmation();
                    }
                });
    }

    private void askForConfirmation() {
        Timber.i("asking for confirmation ...");
        subscriptions.add(view.requestConfirmation(
                R.string.lwire_confirm_header, applicationContext.getString(R.string.lwire_confirm_body), getConfirmButtons())
                .subscribe(new Consumer<BtnReturnBundle>() {
                    @Override
                    public void accept(@NonNull BtnReturnBundle btnReturnBundle) throws Exception {
                        Timber.d("Clicked! Do something");
                    }
                }));
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

    private void loadGroupSelection() {
        Timber.i("loading group selection ... UID = " + currentLiveWireAlertUid);
        subscriptions.add(view.requestSelection(
                R.string.group_select_title,
                realmService.loadObjectsForSelection(Group.class))
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        Timber.i("selected! : " + s);
                        if (!TextUtils.isEmpty(currentLiveWireAlertUid)) {
                            setGroupForAlert(currentLiveWireAlertUid, s);
                        } else {
                            Timber.e("error! selection called without valid LiveWire alert in waiting");
                        }
                    }
                }));
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

    @Override
    public void menuReady() {
        subscriptions.add(view.logoutClicked().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean valid) throws Exception {
                if (valid) {
                    logoutRetainingData();
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
    public BtnGrouping obtainDefaultSubButtons() {
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
                .name("TAKE_AUDIO")
                .actionCode(TAKE_AUDIO_ACTION)
                .drawableRes(R.mipmap.app_icon)
                .labelRes(R.string.take_audio)
                .build();

        return BtnGrouping.builder()
                .firstSubMenu(new BtnParameters[] { takeVideo, takePhoto, takeAudio })
                .build();
    }

    private void handleSubButtonClick(BtnReturnBundle parameters) {
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
                        MediaFile mediaFile = realmService.loadObjectByUid(MediaFile.class, s, false);
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

    private void tryLaunchVideo() {
        if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
            Timber.d("we have permission to record video");
            takeVideo();
        } else {
            view.requestPermission(new String[] {WRITE_EXTERNAL_STORAGE});
        }
    }

    private void takeVideo() {
        /*try {
            Uri outputFileVideo = mediaService.createFileForMedia(".mpeg");
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
    public void handleActivityResult(int requestCode, Intent data) {
        if (requestCode == TAKE_PHOTO_ACTION) {
            Timber.e("photo taken!");
            mediaService
                    .captureMediaFile(currentMediaFileUid, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(@NonNull String s) throws Exception {
                            view.showSuccessMsg("Done!");
                            askForHeadline();
                        }
                    });
        }
    }

    @Override
    public void handleActivityResultError(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void cleanUpForActivity() {
        Timber.i("Cleaning up activity, including closing Realm instance");
        userDetailsService.cleanUpForActivity();
    }
}
