package com.xengine.android.full.media.image;

import android.graphics.Bitmap;
import org.apache.http.HttpResponse;

/**
 * 图片下载接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午4:08
 */
public interface XImageDownloadMgr {

    /**
     * 根据图片url，执行下载，返回HttpResponse
     * @param imgUrl
     * @return
     */
    HttpResponse download(String imgUrl);

    /**
     * 访问网络，把图片下载并保存到本地，返回图片的本地文件名。
     * compress默认为XImageLocalMgr.DEFAULT_COMPRESS.
     * sWidth为屏幕宽，sHeight默认为屏幕高。
     * @param imgUrl
     * @return
     */
    String downloadImg2File(String imgUrl);

    /**
     * 访问网络，把图片下载并保存到本地，返回图片的本地文件名。
     * sWidth为屏幕宽，sHeight默认为屏幕高。
     * @param imgUrl
     * @param compress
     * @return
     */
    String downloadImg2File(String imgUrl, int compress);

    /**
     * 访问网络，把图片下载并保存到本地，返回图片的本地文件名。
     * @param imgUrl
     * @param compress
     * @param sWidth
     * @param sHeight
     * @return 存到本地的文件名
     */
    String downloadImg2File(String imgUrl, int compress, int sWidth, int sHeight);


    /**
     * 访问网络，把图片下载，在内存中处理，并返回bitmap。
     * sWidth为屏幕宽，sHeight默认为屏幕高。
     * @param imgUrl
     * @return
     */
    Bitmap downloadImg2Bmp(String imgUrl);

    /**
     * 访问网络，把图片下载，在内存中处理，并返回bitmap。
     * @param imgUrl
     * @param sWidth
     * @param sHeight
     * @return 存到本地的文件名
     */
    Bitmap downloadImg2Bmp(String imgUrl, int sWidth, int sHeight);
}
