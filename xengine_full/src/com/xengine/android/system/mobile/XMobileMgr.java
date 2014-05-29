package com.xengine.android.system.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jasontujun.
 * Date: 12-11-2
 * Time: 上午10:37
 */
public interface XMobileMgr {

    /**
     * 用来标识请求照相功能的activity
     */
    public static final int CAMERA_WITH_DATA = 3023;

    /**
     * 用来标识请求gallery的activity
     */
    public static final int PHOTO_PICKED_WITH_DATA = 3021;

    /**
     * 用来标识请求二维码的activity
     */
    public static final int SCAN_QR_CODE = 298427;


    /**
     * 清空缓存照片的文件夹
     */
    void clearPhotoDir();

    /**
     * 获取IMEI号
     * @return
     */
    String getIMEI();

    /**
     * 发送短信
     * @param context
     * @param mobileNumber
     * @param content
     */
    void sendMessage(Context context, String mobileNumber, String content);

    /**
     * 请求Camera程序获取照片
     * @param listener
     */
    void doTakePhoto(XPhotoListener listener);

    /**
     * 请求Gallery程序获取照片
     * @param listener
     */
    void doPickPhotoFromGallery(XPhotoListener listener);

    /**
     * 请求扫描程序获取二维码
     * @param scanCodeActivity 扫描程序的类
     * @param listener
     */
    void doScanCode(Class<? extends Activity> scanCodeActivity, XScanCodeListener listener);

    /**
     * 回调函数。
     * 对于启动时调用了startActivityForResult的请求，
     * 结果返回时需要更判断他们各自的requestCode进行相应的处理。
     * @param context
     * @param requestCode
     * @param resultCode
     * @param data
     */
    void onInvokeResult(Context context, int requestCode, int resultCode, Intent data);
}
