package com.oztaking.www.ch1camerarecordvideo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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

    /**
     * 录像机运行的状态机--三个标识
     */
    private int mRecordState;

    public static final int CONSTANT_RECORD_STATE_INIT = 0; //初始化的状态
    public static final int CONSTANT_RECORD_STATE_RECORDING = 1; //录制的状态
    public static final int CONSTANT_RECORD_STATE_PAUSE = 2; //暂停状态

    private MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            if (mMediaRecorder != null){
                mMediaRecorder.reset();
            }
        }
    };
    private String mCurrentVideoFilePath;
    private long mPauseTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera_record_video);
        ButterKnife.bind(this);
        /**
         * 配置surfaceView
         */
        SurfaceViewConfigure();
        Logger.init("CustomCameraRecordVideoActivity");

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
//            stopCamera();
            releaseCamera();
        }


    };


    /**
     * 初始化Camera
     */

    private void initCamera(){
        if (mCamera != null){
            releaseCamera();
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


    /**
     * 视频录制
     */
    @OnClick(R.id.IV_Record_control)
    public void StartRecordVideo(){
        if (mRecordState == CONSTANT_RECORD_STATE_INIT){
            if (createRecordDir() == null){
                return;
            }
            mCurrentVideoFilePath = createRecordDir() + createRecordFileName();

            //开始录制
            if (!startRecordVideo()){
                return;
            }

            refreshControlUI();
            mRecordState = CONSTANT_RECORD_STATE_RECORDING;
        }else if (mRecordState == CONSTANT_RECORD_STATE_RECORDING){
            //停止录制视频
            stopRecordVideo();
            //先给Camera加锁之后再释放相机
            mCamera.lock();
            releaseCamera();

            refreshControlUI();

            mRecordState = CONSTANT_RECORD_STATE_INIT;
            /**
             * 跳转到播放页面,延时1s，如果有视频合成保证视频合成完成
             */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(
                            CustomCameraRecordVideoActivity.this,
                            PlayVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("videoPath",mCurrentVideoFilePath);
                    intent.putExtras(bundle);
                    Logger.i("startActivity 前");
                    startActivity(intent);
                    Logger.i("startActivity 后");
                    finish();
                }
            },1000);

            //ToDo 视频合并
        }else if (mRecordState == CONSTANT_RECORD_STATE_PAUSE){
            //代表视频暂停录制时，点击中心按钮
            Intent intent = new Intent(
                    CustomCameraRecordVideoActivity.this,
                    PlayVideoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("videoPath", mCurrentVideoFilePath);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

    }

    /**
     * 录像暂停
     */
    @OnClick(R.id.IV_Record_Pause)
    public void PauseRecordVideo(){
        if (mRecordState == CONSTANT_RECORD_STATE_RECORDING){
            //取消自动对焦
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success){
                        CustomCameraRecordVideoActivity.this.mCamera.cancelAutoFocus();;
                    }
                }
            });
            //正在录制的视频，点击后暂停
            stopRecordVideo();
            refreshPauseUI();

            //TODO 是否进行视频合并

            mRecordState = CONSTANT_RECORD_STATE_PAUSE;

        }else if(mRecordState == CONSTANT_RECORD_STATE_PAUSE){
            if (createRecordDir() == null){
                return;
            }
            //视频保存路径
            mCurrentVideoFilePath = createRecordDir() + createRecordFileName();
            //继续录制视频
            if (!startRecordVideo()){
              return;
            }

            refreshPauseUI();
            mRecordState = CONSTANT_RECORD_STATE_RECORDING;
        }

    }

    /**
     * 更新暂停视频的UI
     */
    private void refreshPauseUI() {
        if (mRecordState == CONSTANT_RECORD_STATE_RECORDING){
            mIVPause.setImageResource(R.drawable.control_play);
            mPauseTime = SystemClock.elapsedRealtime();
            mChronometerRecordTime.stop();
        }else if (mRecordState == CONSTANT_RECORD_STATE_PAUSE){
            /**
             * 计算暂停之后的时间
             */
            mIVPause.setImageResource(R.drawable.control_pause);
            if (mPauseTime == 0){
                mChronometerRecordTime.setBase(SystemClock.elapsedRealtime());
            }else{
                mChronometerRecordTime.setBase(SystemClock.elapsedRealtime()
                - (mPauseTime - mChronometerRecordTime.getBase()) );
            }

            mChronometerRecordTime.start();
        }
    }

    /**
     * 释放摄像机
     */
    private void releaseCamera() {
        if (mCamera !=null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 点击停止按钮执行的UI更新操作
     */
    private void refreshControlUI() {
        if (mRecordState == CONSTANT_RECORD_STATE_INIT){
            //录像的计时设置
            mChronometerRecordTime.setBase(SystemClock.elapsedRealtime());;
            mChronometerRecordTime.start();

            mIVStart.setImageResource(R.drawable.recordvideo_stop);
            //1s后停止录制按钮?????,防止多次按下录制暂停按钮
            mIVStart.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIVStart.setEnabled(true);
                }
            },1000);
            mIVPause.setVisibility(View.VISIBLE);
            mIVPause.setEnabled(true);

        }else if(mRecordState == CONSTANT_RECORD_STATE_RECORDING){
            mPauseTime =0;
            mChronometerRecordTime.stop();

            mIVStart.setImageResource(R.drawable.recordvideo_start);
            mIVPause.setVisibility(View.GONE);
            mIVPause.setEnabled(false);

        }
    }

    /**
     * 开始录制视频
     */
    private boolean startRecordVideo() {
       /* boolean isCreateOK = createRecordDir();
        if (!isCreateOK){
            return false;
        }*/

        initCamera();
        //录制视频前必须先解锁Camera
        mCamera.unlock();
        setConfigRecord();
        try {
            //开始录制
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

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
        mMediaRecorder.setMaxFileSize(1024*1024*1024);
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
//        mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        mMediaRecorder.setOutputFile(mCurrentVideoFilePath);

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

          /*  mChronometerRecordTime.stop();
            //设置开始按钮可点击，停止按钮不可点击
            mIVStart.setEnabled(true);
            mIVPause.setEnabled(false);
            isRecording = false;*/
        }
    }


    private String createRecordDir() {

        if (!Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())){
            Toast.makeText(this, "sd卡错误", Toast.LENGTH_SHORT).show();
            return null;
        }
        /**
         *保存的文件路径
         */
//        File recordFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Record");

        File directory = Environment.getExternalStorageDirectory();
        File file = new File(directory.toString() + "/RecordVideo/");
        if (!file.exists()){
            file.mkdir();
        }

        return directory.toString()+"/RecordVideo/";

    }

    private String createRecordFileName(){
        /**
         * 保存的文件名称
         */
        String recordFileName = "VID"+new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date())+".mp4";
//        mRecordFile = new File(recordFileDir, recordFileName);
        return recordFileName;
    }




}
