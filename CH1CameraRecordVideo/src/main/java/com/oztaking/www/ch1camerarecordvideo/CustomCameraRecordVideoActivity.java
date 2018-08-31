package com.oztaking.www.ch1camerarecordvideo;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ================================================
 * 版    本：
 * 创建日期：
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class CustomCameraRecordVideoActivity extends Activity{

    /**
     * 存储文件
     */
    private File mRecordFile;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    /**
     * UI
     */
    @BindView(R.id.IV_Record_control)
    ImageView mIVStart;

    @BindView(R.id.IV_Record_Pause)
    ImageView mIVPause;



    @BindView(R.id.id_record_surfaceView)
    SurfaceView mSurfaceView;

    @BindView(R.id.chronometer_recordTime)
    Chronometer mChronometerRecordTime;

    private SurfaceHolder mSurfaceViewHolder;

    /**
     * 标识
     */
    private boolean isPause; //是否暂停
    private boolean isRecording; //是否正在录制
    private long mRecordCurrentTime = 0;


    private MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            if (mMediaRecorder != null){
                mMediaRecorder.reset();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera_record_video);
        ButterKnife.bind(this);
        /**
         * 配置surfaceView
         */
        SurfaceViewConfigure();

    }

    /**
     * 配置surfaceView
     */
    private void SurfaceViewConfigure() {
        mSurfaceViewHolder = mSurfaceView.getHolder();
        // 设置Surface不需要维护自己的缓冲区
        mSurfaceViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //设置分辨率
        mSurfaceViewHolder.setFixedSize(320,280);
        //设置组件不会让屏幕自动关闭
        mSurfaceViewHolder.setKeepScreenOn(true);
        //相机创建回调接口
        mSurfaceViewHolder.addCallback(mCallback);
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback(){

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mSurfaceViewHolder.getSurface() == null) {
                return;
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopCamera();
        }


    };


    /**
     * 初始化Camera
     */

    private void initCamera(){
        if (mCamera != null){
            stopCamera();
        }

        //默认开启后置Camera
//        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera = Camera.open();
        if (mCamera == null){
            Toast.makeText(this,"获取相机失败！",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //相机与SurfaceHolder绑定
            mCamera.setPreviewDisplay(mSurfaceViewHolder);
            //配置CameraParams
            setCameraParams();
            //启动相机预览
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.i("开启相机预览失败！！！");
        }
    }

    //配置CameraParams
    private void setCameraParams() {
        if (mCamera != null){
            Camera.Parameters parameters = mCamera.getParameters();
            //设置相机为竖屏
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
                parameters.set("orientation","portrait");
                mCamera.setDisplayOrientation(90);
            }else{
                parameters.set("orientation","landscape");
                mCamera.setDisplayOrientation(0);
            }

            //设置调焦模式
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            //缩短Recording 启动时间,在预览之前设置才有效
            parameters.setRecordingHint(true);
            //设置电子防抖功能
            if (parameters.isVideoStabilizationSupported()){
                parameters.setVideoStabilization(true);
            }
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 释放摄像头资源
     */
    private void stopCamera() {
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    @OnClick(R.id.IV_Record_control)
    public void StartOrPauseRecordVideo(){
        if (!isRecording){
            //Todo 开始录制视屏
            startRecordVideo();
        }else{
            //Todo 停止视频录制
            stopRecordVideo();
        }
    }

    /**
     * 开始录制视频
     */
    private void startRecordVideo() {
        boolean isCreateOK = createRecordDir();
        if (!isCreateOK){
            return;
        }

        initCamera();
        mCamera.unlock();
        setConfigRecord();

        try {
            //开始录制
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        isRecording = true;
        if (mRecordCurrentTime != 0){
            mChronometerRecordTime.setBase(
                    SystemClock.elapsedRealtime()-(mRecordCurrentTime - mChronometerRecordTime.getBase()));
        }else{
            mChronometerRecordTime.setBase(SystemClock.elapsedRealtime());
        }

        mChronometerRecordTime.start();

    }

    /**
     * 配置Record参数
     */
    private void setConfigRecord() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOnErrorListener(onErrorListener);

        /**
         * 使用surfaceView预览
         */
        mMediaRecorder.setPreviewDisplay(mSurfaceViewHolder.getSurface());

        //1.设置采集声音
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //2.设置采集图像
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //3.设置视频、音频输出格式 MP4
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        //4.设置音频编码格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //5.设置图像编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //6.设置立体声
        mMediaRecorder.setAudioChannels(2);
        //7.设置最大录像时间 单位ms
        mMediaRecorder.setMaxDuration(60*1000);
        //8.设置最大录制的大小 单位字节
        mMediaRecorder.setMaxFileSize(1024*1024);
        //9.设置音频位深
        mMediaRecorder.setAudioEncodingBitRate(44100);
        //10.设置视频编码比特率：1M<VideoEncodingBitRate<2M
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        if (profile.videoBitRate > 2*1024*1024){
            mMediaRecorder.setVideoEncodingBitRate(2*1024*1024);
        }else{
            mMediaRecorder.setVideoEncodingBitRate(1024*1024);
        }
        //11.设置捕获的视频帧速率
        mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        //12.设置选择角度，顺时针方向，默认是逆向90度。此处设置的是保存后的视频的角度
        mMediaRecorder.setOrientationHint(90);
        //13.设置录像的分辨率
        mMediaRecorder.setVideoSize(352,288);
        //14.设置输出文件
        mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());

    }


    /**
     * 结束录制视频
     */
    private void stopRecordVideo() {
        if (isRecording && mMediaRecorder != null){
            //防止出现错误之后崩溃
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            //停止录制
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            //释放资源
            mMediaRecorder.release();
            mMediaRecorder = null;

            mChronometerRecordTime.stop();
            //设置开始按钮可点击，停止按钮不可点击
            mIVStart.setEnabled(true);
            mIVPause.setEnabled(false);
            isRecording = false;
        }
    }


    private boolean createRecordDir() {

        if (!Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())){
            Toast.makeText(this, "sd卡错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        /**
         *保存的文件路径
         */
        File recordFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Record");
        if (!recordFileDir.exists()){
            recordFileDir.mkdirs();
        }

        /**
         * 保存的文件名称
         */
        String recordFileName = "VID"+new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date())+".mp4";
        mRecordFile = new File(recordFileDir, recordFileName);

        return true;
    }




}
