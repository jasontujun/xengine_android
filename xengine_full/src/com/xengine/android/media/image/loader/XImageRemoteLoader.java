package com.xengine.android.media.image.loader;

import android.content.Context;
import android.view.View;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.system.download.XSerialDownloadListener;

/**
 * 图片远程加载器的接口。
 * 特点：
 * 1. 包括本地加载，如果本地没有则下载。
 * 2. 加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * 3. 异步方式加载。
 * @see com.xengine.android.media.image.loader.XImageViewRemoteLoader
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午7:11
 * To change this template use File | Settings | File Templates.
 */
public interface XImageRemoteLoader<T extends View> extends XImageLoader {

    /**
     * 异步加载图片(对ImageView)
     * @param context
     * @param imageUrl
     * @param view
     * @param size
     */
    void asyncLoadBitmap(Context context, String imageUrl,
                         T view, XImageProcessor.ImageSize size,
                         XSerialDownloadListener listener);

    /**
     * 设置imageUrl对应的本地路径。
     * 注意:如果是正在下载和下载错误的图片，
     * 分别有对应XImageLocalUrl.IMG_ERROR和XImageLocalUrl.IMG_LOADING
     * @param imageUrl
     * @param localUrl
     */
    void setLocalImage(String imageUrl, String localUrl);
}
