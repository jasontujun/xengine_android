package com.xengine.android.system.mobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import com.xengine.android.media.image.XAndroidImageLocalMgr;
import com.xengine.android.utils.XLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jasontujun.
 * Date: 12-4-22
 * Time: 下午9:53
 */
public class XAndroidMobileMgr implements XMobileMgr {
    private static final String TAG = "MOBILE_MGR";

    private File photoDir;// 存放照片的文件夹

    private File mCurrentPhotoFile;// 当前照片文件

    private XPhotoListener photoListener;// 照片获取返回的监听

    private XScanCodeListener scanCodeListener;// 扫描二维码返回的监听

    private Activity activity;// 所属的activity

    private TelephonyManager telephonyManager;

    private int screenWidth, screenHeight;


    public XAndroidMobileMgr(Activity activity, int screenWidth, int screenHeight) {
        this.activity = activity;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.telephonyManager = (TelephonyManager) activity.
                getSystemService(Context.TELEPHONY_SERVICE);
    }


    @Override
    public void setPhotoDir(File photoDir) {
        this.photoDir = photoDir;
    }

    @Override
    public File getPhotoDir() {
        return photoDir;
    }

    @Override
    public void clearPhotoDir() {
        if(photoDir == null || !photoDir.exists()) {
            return;
        }

        File[] files = photoDir.listFiles();
        for(int i = 0; i <files.length; i++) {
            String name = files[i].getName();
            if(files[i].delete()) {
                XLog.d(TAG, "删除图片成功：" + name);
            }else {
                XLog.d(TAG, "删除图片失败：" + name);
            }
        }
        if(photoDir.delete()) {
            XLog.d(TAG,"删除临时图片文件夹成功");
        }else {
            XLog.d(TAG,"删除临时图片文件夹失败");
        }
    }

    /**
     * 返回手机的IMEI号
     */
    @Override
    public String getIMEI() {
        return telephonyManager.getDeviceId();
    }

    /**
     * 发送短信
     * @param context
     * @param mobileNumber
     * @param content
     */
    @Override
    public void sendMessage(Context context, String mobileNumber, String content) {
        Uri smsToUri = Uri.parse("smsto:"+mobileNumber);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
        mIntent.putExtra("sms_body", content);
        context.startActivity(mIntent );
    }


    @Override
    public void doTakePhoto(XPhotoListener l) {
        try {
            photoListener = l;

            if(!photoDir.exists()) {
                photoDir.mkdirs();
            }

            String temUriStr = createImgName();
            mCurrentPhotoFile = new File(photoDir, temUriStr);
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));// 输出到的文件目录
            activity.startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            XLog.d(TAG, "启动拍照异常~" + e.getMessage());
            l.onFail();
        }
    }


    @Override
    public void doPickPhotoFromGallery(XPhotoListener l) {
        try {
            photoListener = l;

            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
//            intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", 1);
//            intent.putExtra("aspectY", 1);
//            intent.putExtra("outputX", 80);
//            intent.putExtra("outputY", 80);
            intent.putExtra("return-data", true);
            activity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);// KEY!!
        } catch (ActivityNotFoundException e) {
            XLog.d(TAG, "启动拍照异常~"+e.getMessage());
            l.onFail();
        }
    }


    @Override
    public void doScanCode(Class<? extends Activity> scanCodeActivity, XScanCodeListener listener) {
        scanCodeListener = listener;
        final Intent intent = new Intent(activity, scanCodeActivity);
        activity.startActivityForResult(intent, SCAN_QR_CODE);
    }


    @Override
    public void onInvokeResult(Context context, int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)// 此处的 RESULT_OK 是系统自定义得一个常量
            return;

        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {// 调用Gallery返回的
                if(photoListener == null)
                    return;
                try {
                    Uri originalUri = data.getData();//获得图片的uri
                    if(originalUri == null) {
                        photoListener.onFail();
                        return;
                    }
                    XLog.d(TAG, "相册返回，图片uri：" + originalUri.getPath());

                    // 将bitmap存入一个临时图片文件
                    ContentResolver cr = context.getContentResolver();
                    InputStream is = cr.openInputStream(originalUri);
                    InputStream is2 = cr.openInputStream(originalUri);
                    String imgName = createImgName();
                    boolean result = XAndroidImageLocalMgr.getInstance().processImage2File(
                            is, is2, imgName, 75, screenWidth, screenHeight,
                            new Rect(-1, -1, -1, -1));

                    if(result) {
                        mCurrentPhotoFile = XAndroidImageLocalMgr.getInstance().getImgFile(imgName);
                        photoListener.onSuccess(mCurrentPhotoFile);
                    } else {
                        photoListener.onFail();
                    }
                } catch (FileNotFoundException e) {
                    photoListener.onFail();
                    XLog.d(TAG, "FileNotFoundException!!!");
                } catch (IOException e) {
                    photoListener.onFail();
                    XLog.d(TAG, "IOException!!!");
                } finally {
                    photoListener = null;
                    mCurrentPhotoFile = null;
                }

                break;
            }
            case CAMERA_WITH_DATA: {// 照相机程序返回的,再次调用图片剪辑程序去修剪图片
                if(photoListener == null)
                    return;
                try {
                    if(mCurrentPhotoFile == null || (!mCurrentPhotoFile.exists())) {
                        XLog.d(TAG, "拍照返回失败！！");
                        photoListener.onFail();
                        return;
                    }
                    // 对图片进行处理
                    photoListener.onSuccess(mCurrentPhotoFile);
                } finally {
                    photoListener = null;
                    mCurrentPhotoFile = null;
                }
                break;
            }
            case SCAN_QR_CODE: {
                if(scanCodeListener != null) {
                    scanCodeListener.onFail();
                }
                try {
                    Bundle bundle = data.getExtras();
                    if(bundle == null) {
                        scanCodeListener.onFail();
                    }else {
                        String content = bundle.getString("result");
                        XLog.d(TAG, content);
                        scanCodeListener.onSuccess(content);
                    }
                } finally {
                    scanCodeListener = null;
                }
                break;
            }
        }
    }

    /**
     * 生成图片名（保证唯一性）
     * @return
     */
    private String createImgName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyy_MM_dd_HH_mm_ss");
        return dateFormat.format(date) + ".jpg";
    }

}
