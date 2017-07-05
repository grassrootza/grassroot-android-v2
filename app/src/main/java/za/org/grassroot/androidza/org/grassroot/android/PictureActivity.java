package za.org.grassroot.androidza.org.grassroot.android;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureActivity extends AppCompatActivity {

    private Button cameraBtn,galleryBtn;
    private ImageView imageView;
    private String dir;
    private File newfile;
    private String mediaPath;
    private static final int TAKE_PHOTO_FROM_CAMERA_CODE=0;
    private static final int TAKE_PHOTO_FROM_GALLERY_CODE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        cameraBtn= (Button) findViewById(R.id.cameraBtn);
        galleryBtn= (Button) findViewById(R.id.galleryBtn);
        imageView= (ImageView) findViewById(R.id.preview);
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Grassroot/";
        Log.e("Error",dir);
        File newdir = new File(dir);
        newdir.mkdirs();

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String file = dir+timeStamp+".jpg";
                newfile = new File(file);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                }

                Uri outputFileUri = Uri.fromFile(newfile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, TAKE_PHOTO_FROM_CAMERA_CODE);
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, TAKE_PHOTO_FROM_GALLERY_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == TAKE_PHOTO_FROM_CAMERA_CODE && resultCode == RESULT_OK) {

                Bitmap photo = BitmapFactory.decodeFile(newfile.getAbsolutePath());
                imageView.setImageBitmap(photo);


            }
            else if (requestCode == TAKE_PHOTO_FROM_GALLERY_CODE && resultCode == RESULT_OK&&null!=data)
            {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mediaPath = cursor.getString(columnIndex);
                imageView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                // Set the Image in ImageView for Previewing the Media

                cursor.close();
            }
            else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong"+e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
