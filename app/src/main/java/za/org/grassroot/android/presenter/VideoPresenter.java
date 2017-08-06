package za.org.grassroot.android.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

import za.org.grassroot.android.view.VideoActivityView;

public class VideoPresenter implements VideoActivityView.presenter {
    private final VideoActivityView.view view;
    private File newfile;
    private Activity activity;
    public VideoPresenter(VideoActivityView.view view)
    {
        this.view=view;
    }

    @Override
    public void onTakeVideoBtn(Activity activity, String filePath) {
        newfile = new File(filePath);

        try {
            newfile.createNewFile();
        }
        catch (IOException e)
        {
            view.showToastMessage(""+e.getMessage());
        }

        Uri outputFileUri = Uri.fromFile(newfile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        activity.startActivityForResult(cameraIntent, 0);
    }
}
