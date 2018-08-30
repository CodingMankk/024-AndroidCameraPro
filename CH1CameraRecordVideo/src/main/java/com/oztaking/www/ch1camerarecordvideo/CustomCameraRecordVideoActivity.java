package com.oztaking.www.ch1camerarecordvideo;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

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
    @BindView(R.id.Btn_Record_control)
    Button mBtnRecordControl;

    @BindView(R.id.Btn_RestartVideo)
    Button mBtnRestartVideo;

    @BindView(R.id.id_record_surfaceView)
    SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

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
        SurfaceHolder mSurfaceViewHolder = mSurfaceView.getHolder();
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

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

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
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null){
            Toast.makeText(this,"获取相机失败！",Toast.LENGTH_SHORT).show();
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
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

    @OnClick(R.id.Btn_Record_control)
    public void StartRecordVideo(){
        if (!isRecording){
            //Todo 开始录制视屏
        }else{
            //Todo 停止视频录制
        }
    }


}
