package za.org.grassroot2.view;

import android.net.Uri;

import io.reactivex.Observable;
import za.org.grassroot2.model.dto.BtnGrouping;
import za.org.grassroot2.model.dto.BtnReturnBundle;

public interface MainView extends LoggedInView {

    Observable<Boolean> ensureExternalStoragePermission();
    Observable<Boolean> ensureAudioRecordingPermissions();

    void askForHeadline();
    void askForMediaFile();
    void loadGroupSelection();
    void askForDescription();
    void askForConfirmation();
    void goToDefaultScreen();
    void cameraForResult(Uri output, String s, int requestCode);
}