package za.org.grassroot2.view;

import android.app.Activity;
import android.os.Bundle;

import za.org.grassroot.messaging.dto.MessageDTO;

public interface GrassrootView extends ProgressBarContainer {


    Activity getActivity();
    void showSuccessMsg(int successMsg);
    void showErrorSnackbar(int errorTextRes);
    void closeKeyboard();
    void launchActivity(Class<?> cls, Bundle args);

    void handleNoConnection();
    void handleNoConnectionUpload();
    void showNoConnectionMessage();

    void showMessageDialog(String text);

    void hideKeyboard();

    void sendCGMMessage(MessageDTO messageDTO);
}