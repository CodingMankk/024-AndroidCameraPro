package com.oztaking.www.ch1camerarecordvideo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @function:使用系统的Camera录制视频
 *
 * 参考文章地址：https://blog.csdn.net/android_technology/article/details/69388902
 *
 *@author  ozTaking
 */

public class SystemCameraRecordVideoActivity extends AppCompatActivity {

    private static final int RECORD_SYSTEM_VIDEO = 1;

    @BindView(R.id.id_Btn_camera_record_video)
    Button mBtnCameraRecordVideo;

    @BindView(R.id.Btn_RestartVideo)
    Button mBtnRestartVideo;

    @BindView(R.id.id_videoView_record_system)
    VideoView mVideoView;



    private File outMediaFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerarecordvideo);

        ButterKnife.bind(this);

    }

    @OnClick(R.id.id_Btn_camera_record_video)
    public void recordBySystemCamera() {
        Uri fileUri = Uri.fromFile(getOutMediaFile());
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);//限制的录制时长 以秒为单位
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);//设置拍摄的质量最小是0，最大是1（建议不要设置中间值，不同手机似乎效果不同。。。）
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,1024*1024);//限制视频文件大小 以字节为单位
        startActivityForResult(intent,RECORD_SYSTEM_VIDEO);
    }

    @OnClick(R.id.Btn_RestartVideo)
    public void restartVideo(){
        mVideoView.seekTo(0);
        mVideoView.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK){
            return;
        }

        switch(requestCode){
             case RECORD_SYSTEM_VIDEO:
                 mVideoView.setVideoURI(data.getData());
                 if (!mVideoView.isPlaying()){
                     mVideoView.start();
                 }
                  break;
             default:
                  break;
        }


    }

    public File getOutMediaFile() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this,"请检查SDCard",Toast.LENGTH_SHORT).show();;
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DCIM), "SystemCameraApp");

        if (!mediaStorageDir.exists()){
            mediaStorageDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID" +
                timeStamp + ".mp4");
        return mediaFile;
    }
}
