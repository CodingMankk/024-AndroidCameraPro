package com.codingmankk.www.androidcamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author ozTaking
 * <p>
 * 博客参考地址：https://blog.csdn.net/harvic880925/article/details/43163175
 */
public class CamerCaptureCropActivity extends AppCompatActivity {

    private static final int RESUTL_CAMERA_ONLY = 100; //[1-1] 拍照剪裁照片
    private static final int RESULT_CAMERA_CROP_PATH_RESULT = 101; //[1-2] 拍照剪裁照片

    private static final int RESULT_ALBUM_ONLY_THROUGH_DATA = 200; //相册选取照片-缩略图

    private static final int RESULT_ALBUM_CORP_URI_RESULT = 303; //相册选取照片-剪裁-缩略图

    private static final int RESULT_ALBUM_CORP_PATH_RESULT = 401; //相册选取照片-剪裁-缩略图
    private static final int RESULT_ALBUM_CORP_PATH = 400; //相册选取照片-剪裁-缩略图
    /**
     * 存放拍照结果
     */
    private Uri mImageUri;

    /**
     * 存放剪裁结果
     */
    private Uri mImageCropUri;

    //[1] 拍照并剪裁图片
    @BindView(R.id.Btn_Capture)
    Button mBtnCapture;

    //[2]相册选取图片-缩略图
    @BindView(R.id.Btn_SelectImageResSmall)
    Button mBtnSelectImageResSmall;

    //[3]相册选取图片-剪裁-缩略图-uri
    @BindView(R.id.Btn_Select_Album_Crop_Uri)
    Button mBtnSelect_Album_Crop_Uri;

    //[4]相册选取图片-剪裁-缩略图-path
    @BindView(R.id.Btn_Select_Album_Crop_Path)
    Button mSelect_Album_Crop_Path;


    @BindView(R.id.IV_ImageView)
    ImageView mIV;
    private File tempFile;


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
//        String path = getSDCardPath();
        String path = getAbsPath();

        File file = new File(path + "/temp.jpg");
        mImageUri = Uri.fromFile(file);

        File cropFile = new File(getSDCardPath() + "/temp_crop.jpg");
        mImageCropUri = Uri.fromFile(cropFile);

    }

    private String getAbsPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
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
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESUTL_CAMERA_ONLY);
    }

    /**
     * [2]相册选取照片-缩略图
     */
    @OnClick(R.id.Btn_SelectImageResSmall)
    public void mBtnSelectImageResSmall() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_ALBUM_ONLY_THROUGH_DATA);
    }

    /**
     * [3]相册选取照片-剪裁-缩略图-uri--显示不出来图片!!!!!失败
     */
    @OnClick(R.id.Btn_Select_Album_Crop_Uri)
    public void mBtnSelectAlbumCrop() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 1000);
        intent.putExtra("outputY", 1000);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULT_ALBUM_CORP_URI_RESULT);
    }

    /**
     * [4] 相册选取照片-剪裁-缩略图-path
     */
    @OnClick(R.id.Btn_Select_Album_Crop_Path)
    public void BtnSelectAlbumCropPath() {
        /**
         * 启动相册
         */
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_ALBUM_CORP_PATH);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case RESUTL_CAMERA_ONLY: //[1-1] 拍照剪裁照片
                // decodeBitmapSetImageView();
                cropImg(mImageUri);
                break;
            case RESULT_CAMERA_CROP_PATH_RESULT://[1-2] 拍照剪裁照片
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = null;
                    try {
                        InputStream in;
                        in = getContentResolver().openInputStream
                                (mImageCropUri);
                        bitmap = BitmapFactory.decodeStream(in);
                        Logger.i(bitmap + "");
                        mIV.setImageBitmap(bitmap);
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case RESULT_ALBUM_ONLY_THROUGH_DATA: //[2]相册选取照片-缩略图
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    if (bitmap != null) {
                        Bitmap bitmap1 = setScaleBitmap(bitmap, 2);
                        mIV.setImageBitmap(bitmap1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case RESULT_ALBUM_CORP_URI_RESULT://[3-1]
                try {
//                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
                    String path = parsePicPath(CamerCaptureCropActivity.this, data.getData());
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if (bitmap != null) {
                        Bitmap newBitmap = setScaleBitmap(bitmap, 2);
                        mIV.setImageBitmap(newBitmap);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case RESULT_ALBUM_CORP_PATH: //[4-1]
                String path = parsePicPath(CamerCaptureCropActivity.this, data.getData());
                File file = new File(path);
                Uri uri = Uri.fromFile(file);
                cropImgPath(uri);
                break;
            case RESULT_ALBUM_CORP_PATH_RESULT: //[4-2]
                Bundle extra = data.getExtras();
//                if (extra != null){
                if (getTempFile() != null){
                    Bitmap bitmap = BitmapFactory.decodeFile(getTempFile().getAbsolutePath(), null);
                    if (bitmap != null){
                        Bitmap scaleBitmap = setScaleBitmap(bitmap, 2);
                        mIV.setImageBitmap(scaleBitmap);
                    }
                }
                break;
            default:
                break;

        }

    }

    private void cropImgPath(Uri uri) {
        File tempFile = getTempFile();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 700);
        intent.putExtra("outputY", 700);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULT_CAMERA_CROP_PATH_RESULT);
    }

    @SuppressLint("NewApi")
    private String parsePicPath(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocumentsUri(uri)) {

                String docId = DocumentsContract.getDocumentId(uri);
                String[] splits = docId.split(":");
                String type = splits[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + File.separator + splits[1];
                }
            } else if (isDownloadsDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            }

            // MediaDocumentsUri
            else if (isMediaDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosContentUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;


    }

    private static boolean isExternalStorageDocumentsUri(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocumentsUri(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocumentsUri(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosContentUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            try {
                if (cursor != null)
                    cursor.close();
            } catch (Exception e) {
                Log.e("ozTaking",e.getMessage());
            }
        }
        return null;

    }

    private Bitmap setScaleBitmap(Bitmap bitmap, int scale) {

        if (bitmap != null) {
            int sourceWidth = bitmap.getWidth();
            int sourceHeight = bitmap.getHeight();
            Matrix matrix = new Matrix();
            int cropWidth = sourceWidth / scale;
            int cropHeight = sourceHeight / scale;

            float scaleWidth = (float) cropWidth / sourceWidth;
            float scaleHeight = (float) cropHeight / sourceHeight;

            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, true);
            /**
             * 释放原始图片占用的内存，防止out of memory异常发生
             */
            bitmap.recycle();
            return newBitmap;

        }
        return null;
    }

    private void cropImg(Uri imageUri) {
        Intent intent = null;
        intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 700);
        intent.putExtra("outputY", 700);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULT_CAMERA_CROP_PATH_RESULT);

    }

    private void decodeBitmapSetImageView(Uri mImageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(mImageUri));
            Logger.i(bitmap + "");
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

    public File getTempFile() {

        try {
            return new File(getSDCardPath()+"/temp_crop.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
