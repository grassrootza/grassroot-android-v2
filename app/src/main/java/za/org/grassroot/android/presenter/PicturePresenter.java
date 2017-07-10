package za.org.grassroot.android.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import za.org.grassroot.android.view.PictureView;

/**
 * Created by Pial on 10-Jul-17.
 */

public class PicturePresenter implements PictureView.presenter {
    private final PictureView.view view;
    private File newfile;
    public PicturePresenter(PictureView.view view)
    {
        this.view=view;
    }
    @Override
    public File onCameraBtnClick(Activity activity, String filePath) {
        newfile = new File(filePath);
        try {
            newfile.createNewFile();
        }
        catch (IOException e)
        {
            view.showToastMessage(""+e.getMessage());
        }

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        activity.startActivityForResult(cameraIntent, 0);
        return newfile;

    }

    @Override
    public void onGalleryBtnClick(Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(galleryIntent, 1);

    }
}
