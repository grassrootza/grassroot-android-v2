package za.org.grassroot.android.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.reactivex.annotations.NonNull;
import timber.log.Timber;
import za.org.grassroot.android.R;
import za.org.grassroot.android.presenter.MainPresenter;
import za.org.grassroot.android.services.auth.GrassrootAuthUtils;
import za.org.grassroot.android.view.MainView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends GrassrootActivity implements MainView.view {

    public static final int RequestPermissionCode = 1;
    private MainPresenter mainPresenter;
    private Button videoBtn,audioBtn,pictureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoBtn= (Button) findViewById(R.id.videoBtn);
        audioBtn= (Button) findViewById(R.id.audioBtn);
        pictureBtn= (Button) findViewById(R.id.pictureBtn);
        mainPresenter=new MainPresenter(this);


        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission())
                {
                    mainPresenter.onVideoBtnClick();

                }
                else {
                    requestPermission(MainActivity.this);
                }
            }
        });
        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission())
                {
                    mainPresenter.onAudioBtnClick();
                }
                else {
                    requestPermission(MainActivity.this);
                }
            }
        });
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission())
                {
                    mainPresenter.onPictureBtnClick();
                }
                else {
                    requestPermission(MainActivity.this);
                }
            }
        });

        // just for testing for now
        String token = GrassrootAuthUtils.getToken();
        Timber.i("Token stored: " + token);
    }

    private void requestPermission(Activity act) {
        ActivityCompat.requestPermissions(act, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    mainPresenter.permissionStatus(StoragePermission,RecordPermission);
                }
                break;
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void gotoVideoActivity() {
        startActivity(new Intent(MainActivity.this,VideoActivity.class));
        finish();
    }

    @Override
    public void gotoAudioActivity() {
        startActivity(new Intent(MainActivity.this,AudioActivity.class));
        finish();

    }

    @Override
    public void gotoPictureActivity() {
        startActivity(new Intent(MainActivity.this,PictureActivity.class));
        finish();
    }

    @Override
    public void showToastMessage(String msg) {
        Toast.makeText(this, ""+msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgressBar() {
        
    }

    @Override
    public void closeProgressBar() {

    }
}

