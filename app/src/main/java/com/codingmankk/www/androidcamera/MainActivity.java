package com.codingmankk.www.androidcamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author ozTaking
 *
 * 博客参考地址：https://blog.csdn.net/harvic880925/article/details/43163175
 */
public class MainActivity extends AppCompatActivity {

    private static final int RESUTL_CAMERA_ONLY = 100;
    private static final int RESUTL_CAMERA_CROP = 301;
    private Uri mImageUri;

    @BindView(R.id.Btn_Capture)
    Button mBtnCapture;

    @BindView(R.id.Btn_CaptureAndCrop)
    Button mBtnCaptureAndCrop;


    @BindView(R.id.IV_ImageView)
    ImageView mIV;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * ButterKnife 绑定
         */
        ButterKnife.bind(this);
        initData();

    }

    private void initData() {
        String path = getSDCardPath();
        File file = new File(path + "/temp.jpg");
        mImageUri = Uri.fromFile(file);

    }

    /**
     * [1]点击之后启动相机
     */
    @OnClick({R.id.Btn_Capture})
    public void takeCameraOnly() {
        Intent intent = null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESUTL_CAMERA_ONLY);
    }

    /**
     * [2] 启动相机并剪裁
     */
    @OnClick(R.id.Btn_CaptureAndCrop)
    public void takeCameraAndCrop(){
        Intent intent =null;
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("crop",true);
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",1000);
        intent.putExtra("outputY",1000);
        intent.putExtra("scale",true);
        intent.putExtra("return-data",false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageUri);
        intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);
        startActivityForResult(intent,RESUTL_CAMERA_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case RESUTL_CAMERA_ONLY:
                decodeBitmapSetImageView();
                break;
            case RESUTL_CAMERA_CROP:
                mIV.setImageBitmap(null);
                decodeBitmapSetImageView();
                break;
            default:
                break;
        }

    }

    private void decodeBitmapSetImageView() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(mImageUri));
            Logger.i(bitmap+"");
            mIV.setImageBitmap(bitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }

    public String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        /**
         *  返回与当前 Java 应用程序相关的运行时对象
         */
        Runtime runtime = Runtime.getRuntime();

        try {
            /**
             *  启动另一个进程来执行命令
             */
            Process p = runtime.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String lineStr;

            while ((lineStr = br.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure", "");
                        return result;
                    }
                }


                // 检查命令是否执行失败
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                }

            }
            br.close();
            in.close();

        } catch (Exception e) {
            return Environment.getExternalStorageDirectory().getPath();
        }

        return Environment.getExternalStorageDirectory().getPath();
    }
}
