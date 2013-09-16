package com.xengine.android.media.image.download;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import com.xengine.android.media.image.processor.XAndroidImageProcessor;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.download.XHttpDownloadMgr;
import com.xengine.android.session.http.XBufferedHttpResponse;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;

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

    public XHttpImageDownloadMgr(XHttp httpClient) {
        super(httpClient);
    }

    @Override
    public String downloadImg2File(String imgUrl, String format) {
        return downloadImg2File(
                imgUrl,
                format,
                XImageProcessor.DEFAULT_COMPRESS,
                0,
                0);
    }


    @Override
    public String downloadImg2File(String imgUrl, String format, int compress) {
        return downloadImg2File(
                imgUrl,
                format,
                compress,
                0,
                0);
    }


    @Override
    public String downloadImg2File(String imgUrl, String format,
                                   int compress, int sWidth, int sHeight) {
        if (mListener != null)
            mListener.onStart(imgUrl);

        XHttpRequest request = mHttpClient.newRequest(imgUrl);
        XHttpResponse response = mHttpClient.execute(request);
        if (response == null) {
            if (mListener != null)
                mListener.onError(imgUrl, "No Response");
            return null;
        }

        // 处理GIF图片（TODO 还有问题）
//        if(imgUrl.endsWith("gif") || imgUrl.endsWith("GIF")) {
//            return XAndroidImageMgr.getInstance().processGif2File(response);
//        }
        try {
            XBufferedHttpResponse bufferedHttpResponse = new XBufferedHttpResponse(response);
            InputStream is = bufferedHttpResponse.getContent();
            InputStream is2 = bufferedHttpResponse.getContent();
            // 确定图片格式
            String imgFormat = format;
            if (TextUtils.isEmpty(format)) {
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
            // 处理并生产图片文件
            boolean processResult = XAndroidImageProcessor.getInstance().
                    processImage2File(is, is2, imgName, sWidth, sHeight,
                            new Rect(-1, -1, -1, -1), cFormat, compress);
            bufferedHttpResponse.consumeContent();
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
        return downloadImg2Bmp(imgUrl, format, 0, 0);
    }

    @Override
    public Bitmap downloadImg2Bmp(String imgUrl, String format, int sWidth, int sHeight) {
        if (mListener != null)
            mListener.onStart(imgUrl);

        XHttpRequest request = mHttpClient.newRequest(imgUrl);
        XHttpResponse response = mHttpClient.execute(request);
        if (response == null) {
            if (mListener != null)
                mListener.onError(imgUrl, "No Response");
            return null;
        }

        try {
            XBufferedHttpResponse bufferedHttpResponse = new XBufferedHttpResponse(response);
            InputStream is = bufferedHttpResponse.getContent();
            InputStream is2 = bufferedHttpResponse.getContent();
            Bitmap result = XAndroidImageProcessor.getInstance().
                    processImage2Bmp(is, is2, sWidth, sHeight, new Rect(-1, -1, -1, -1));
            bufferedHttpResponse.consumeContent();
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
