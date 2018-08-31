package com.oztaking.www.ch1camerarecordvideo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oztaking.www.ch1camerarecordvideo.util.mMediaController;
import com.oztaking.www.ch1camerarecordvideo.view.CustomMediaPlayer;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/***********************************************
 * 文 件 名: 
 * 创 建 人: OzTaking
 * 功    能：
 * 创建日期: 
 * 修改时间：
 * 修改备注：
 ***********************************************/
public class PlayVideoActivity extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,mMediaController.MediaPlayerControl{

    @BindView(R.id.id_Custom_Player_VideoView)
    CustomMediaPlayer mVideoView;

    private mMediaController controller;
    private String mVideoPath;
    private mMediaController mController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.now_media_controller);

        ButterKnife.bind(this);

        setVideoViewRatio();

        setVideoViewUrl();
        initeView();


    }

    private void initeView() {
        //设置就绪状态
        mVideoView.setOnPreparedListener(this);
        //创建MediaController
        mController = new mMediaController(this);
    }

    //设置videoView的播放地址
    private void setVideoViewUrl() {
        /**
         * 取出传递的地址
         */
        mVideoPath = getIntent().getExtras().getString("VideoPath");
        File sourceVideoFile = new File(mVideoPath);
        //设置播放的地址
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED
        )){
            mVideoView.setVideoURI(Uri.fromFile(sourceVideoFile));
        }
    }

    /**
     * 设置播放器的宽高比是：4:3
     */
    private void setVideoViewRatio() {
        //设置播放的比例是4：3
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoView
                .getLayoutParams();
        params.width = screenWidth;
        params.height = screenWidth*4 /3;
        params.gravity = Gravity.TOP;
        mVideoView.setLayoutParams(params);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mController.setMediaPlayer(this);
        mController.setAnchorView((ViewGroup) findViewById(R.id.fl_videoView_parent)        );
        mController.show();
    }

    @Override
    public void start() {
        mVideoView.start();
    }

    @Override
    public void pause() {
        if (mVideoView.isPlaying()){
            mVideoView.pause();
        }
    }

    @Override
    public int getDuration() {
        return mVideoView.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mVideoView.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return mVideoView.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return mVideoView.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return mVideoView.canSeekForward();
    }

    @Override
    public boolean isFullScreen() {
        return mVideoView.isFullScreen();
    }

    @Override
    public void toggleFullScreen() {

    }
}
