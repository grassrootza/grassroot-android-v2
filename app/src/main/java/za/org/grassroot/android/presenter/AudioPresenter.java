package za.org.grassroot.android.presenter;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

import za.org.grassroot.android.view.AudioView;

/**
 * Created by Pial on 10-Jul-17.
 */

public class AudioPresenter implements AudioView.presenter {
    private final AudioView.view view;
    private MediaRecorder mediaRecorder;
    private String audioSavePathInDevice;
    private MediaPlayer mediaPlayer;
    public AudioPresenter(AudioView.view view)
    {
        this.view=view;
    }


    @Override
    public void onButtonStart(MediaRecorder mediaRecorder, String audioSavePathInDevice) {
        this.mediaRecorder=mediaRecorder;
        this.audioSavePathInDevice=audioSavePathInDevice;
        MediaRecorderReady();
        try {
            this.mediaRecorder.prepare();
            this.mediaRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        view.showToastMessage("Recording started");
    }

    @Override
    public void onButtonStop() {
        this.mediaRecorder.stop();
        view.showToastMessage("Recording Completed");

    }

    @Override
    public void onButtonPlayLastRecordAudio(MediaPlayer mediaPlayer) throws IllegalArgumentException,
            SecurityException, IllegalStateException {
        this.mediaPlayer=mediaPlayer;
        this.mediaPlayer = new MediaPlayer();
        try {
            this.mediaPlayer.setDataSource(audioSavePathInDevice);
            this.mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mediaPlayer.start();

        view.showToastMessage("Recording Playing");
    }

    @Override
    public void onButtonStopPlayingRecording() {
        if (mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            MediaRecorderReady();
        }
    }


    private void MediaRecorderReady() {
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioSavePathInDevice);
    }




}
