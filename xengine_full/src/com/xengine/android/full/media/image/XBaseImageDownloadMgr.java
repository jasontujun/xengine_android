package com.xengine.android.full.media.image;

import android.graphics.Bitmap;
import android.graphics.Rect;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片下载管理器。
 * Created by jasontujun.
 * Date: 12-2-27
 * Time: 下午8:03
 */
public abstract class XBaseImageDownloadMgr implements XImageDownloadMgr {

    private int screenWidth, screenHeight;

    public XBaseImageDownloadMgr(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }


    @Override
    public String downloadImg2File(String imgUrl) {
        return downloadImg2File(imgUrl, XImageLocalMgr.DEFAULT_COMPRESS, screenWidth, screenHeight);
    }


    @Override
    public String downloadImg2File(String imgUrl, int compress) {
        return downloadImg2File(imgUrl, compress, screenWidth, screenHeight);
    }


    @Override
    public String downloadImg2File(String imgUrl, int compress, int sWidth, int sHeight) {
        HttpResponse response = download(imgUrl);
        if(response == null)
            return null;

        // 处理GIF图片（TODO 还有问题）
//        if(imgUrl.endsWith("gif") || imgUrl.endsWith("GIF")) {
//            return XAndroidImageMgr.getInstance().processGif2File(response);
//        }else {

        // 处理非GIF图片
        try {
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufferedHttpEntity.getContent();
            InputStream is2 = bufferedHttpEntity.getContent();
            // 生成文件名
            String imgName = "" + System.currentTimeMillis() + ".png";
            if(XAndroidImageLocalMgr.getInstance().
                    processImage2File(is, is2, imgName, compress,
                            sWidth, sHeight, new Rect(-1, -1, -1, -1))) {
                entity.consumeContent();;
                return imgName;
            }else {
                entity.consumeContent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }

        return null;
    }


    @Override
    public Bitmap downloadImg2Bmp(String imgUrl) {
        return downloadImg2Bmp(imgUrl, screenWidth, screenHeight);
    }

    @Override
    public Bitmap downloadImg2Bmp(String imgUrl, int sWidth, int sHeight) {
        HttpResponse response = download(imgUrl);
        if(response == null)
            return null;

        try {
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream is = bufferedHttpEntity.getContent();
            InputStream is2 = bufferedHttpEntity.getContent();
            Bitmap result = XAndroidImageLocalMgr.getInstance().
                    processImage2Bmp(is, is2, sWidth, sHeight,
                            new Rect(-1, -1, -1, -1));
            entity.consumeContent();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
