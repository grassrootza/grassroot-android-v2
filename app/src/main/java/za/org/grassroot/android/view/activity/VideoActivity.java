package za.org.grassroot.android.view.activity;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import za.org.grassroot.android.R;
import za.org.grassroot.android.presenter.VideoPresenter;
import za.org.grassroot.android.view.VideoActivityView;

public class VideoActivity extends AppCompatActivity implements VideoActivityView.view {

    private String dir;
    private VideoView videoView;
    private File newfile;
    private String file;
    private VideoPresenter videoPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);videoView= (VideoView) findViewById(R.id.video);
        videoPresenter=new VideoPresenter(this);
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Grassroot/";
        File newdir = new File(dir);
        newdir.mkdirs();
    }

    public void takeVideo(View view) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        file = dir+timeStamp+".jpg";
        videoPresenter.onTakeVideoBtn(this,file);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK)
        {
            videoView.setVideoPath(file);
            videoView.start();
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    public void showToastMessage(String msg) {
        Toast.makeText(this, ""+msg, Toast.LENGTH_SHORT).show();

    }
}
