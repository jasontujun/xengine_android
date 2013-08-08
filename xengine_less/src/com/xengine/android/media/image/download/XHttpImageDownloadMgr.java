package com.xengine.android.media.image.download;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.xengine.android.media.image.processor.XAndroidImageProcessor;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.download.XHttpDownloadMgr;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.utils.XStringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片下载管理器。
 * Created by jasontujun.
 * Date: 12-2-27
 * Time: 下午8:03
 */
public final class XHttpImageDownloadMgr extends XHttpDownloadMgr
        implements XImageDownload {

    private int mScreenWidth, mScreenHeight;

    public XHttpImageDownloadMgr(XHttp httpClient, int screenWidth, int screenHeight) {
        super(httpClient);
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
    }


    @Override
    public String downloadImg2File(String imgUrl, String format) {
        return downloadImg2File(
                imgUrl,
                format,
                XImageProcessor.DEFAULT_COMPRESS,
                mScreenWidth,
                mScreenHeight);
    }


    @Override
    public String downloadImg2File(String imgUrl, String format, int compress) {
        return downloadImg2File(
                imgUrl,
                format,
                compress,
                mScreenWidth,
                mScreenHeight);
    }


    @Override
    public String downloadImg2File(String imgUrl, String format,
                                   int compress, int sWidth, int sHeight) {
        if (mListener != null)
            mListener.onStart(imgUrl);

        HttpGet httpGet = new HttpGet(imgUrl);
        HttpResponse response = mHttpClient.execute(httpGet, false);
        if (response == null) {
            if (mListener != null)
                mListener.onError(imgUrl, "No Response");
            return null;
        }

        // 处理GIF图片（TODO 还有问题）
//        if(imgUrl.endsWith("gif") || imgUrl.endsWith("GIF")) {
//            return XAndroidImageMgr.getInstance().processGif2File(response);
//        }
        HttpEntity entity = response.getEntity();
        try {
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufferedHttpEntity.getContent();
            InputStream is2 = bufferedHttpEntity.getContent();
            // 确定图片格式
            String imgFormat = format;
            if (XStringUtil.isNullOrEmpty(format)) {
                String lowerImgUrl = imgUrl.toLowerCase();
                if (lowerImgUrl.endsWith(FORMAT_JPG))
                    imgFormat = FORMAT_JPG;
                else if (lowerImgUrl.endsWith(FORMAT_JPEG))
                    imgFormat = FORMAT_JPEG;
                else if (lowerImgUrl.endsWith(FORMAT_PNG))
                    imgFormat = FORMAT_PNG;
                else if (lowerImgUrl.endsWith(FORMAT_GIF))
                    imgFormat = FORMAT_GIF;
                else
                    imgFormat = DEFAULT_FORMAT;
            }
            // 生成文件名
            String imgName = "IMAGE_" + System.currentTimeMillis() + "." + imgFormat;
            Bitmap.CompressFormat cFormat = Bitmap.CompressFormat.JPEG;
            if (imgFormat.equals(FORMAT_PNG))
                cFormat = Bitmap.CompressFormat.PNG;
            boolean processResult = XAndroidImageProcessor.getInstance().
                    processImage2File(is, is2, imgName, sWidth, sHeight,
                            new Rect(-1, -1, -1, -1), cFormat, compress);
            entity.consumeContent();
            String result = processResult ? imgName : null;
            if (mListener != null)
                mListener.onComplete(imgUrl, result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onError(imgUrl, "IO Exception");
        }
        return null;
    }


    @Override
    public Bitmap downloadImg2Bmp(String imgUrl, String format) {
        return downloadImg2Bmp(imgUrl, format, mScreenWidth, mScreenHeight);
    }

    @Override
    public Bitmap downloadImg2Bmp(String imgUrl, String format, int sWidth, int sHeight) {
        if (mListener != null)
            mListener.onStart(imgUrl);

        HttpGet httpGet = new HttpGet(imgUrl);
        HttpResponse response = mHttpClient.execute(httpGet, false);
        if (response == null) {
            if (mListener != null)
                mListener.onError(imgUrl, "No Response");
            return null;
        }

        try {
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufferedHttpEntity.getContent();
            InputStream is2 = bufferedHttpEntity.getContent();
            Bitmap result = XAndroidImageProcessor.getInstance().
                    processImage2Bmp(is, is2, sWidth, sHeight,
                            new Rect(-1, -1, -1, -1));
            entity.consumeContent();
            if (mListener != null)
                mListener.onComplete(imgUrl, null);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onError(imgUrl, "IO Exception");
        }
        return null;
    }

}
