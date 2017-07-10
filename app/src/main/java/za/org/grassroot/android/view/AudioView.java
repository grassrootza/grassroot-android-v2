package za.org.grassroot.android.view;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

/**
 * Created by Pial on 10-Jul-17.
 */

public interface AudioView {

    interface view{
        void showToastMessage(String msg);
    }
    interface presenter{
        void onButtonStart(MediaRecorder mediaRecorder, String audioSavePathInDevice);
        void onButtonStop();
        void onButtonPlayLastRecordAudio(MediaPlayer mediaPlayer);
        void onButtonStopPlayingRecording();
    }
}
