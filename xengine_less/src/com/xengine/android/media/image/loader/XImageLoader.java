package com.xengine.android.media.image.loader;

import android.content.Context;
import android.graphics.Bitmap;
import com.xengine.android.media.image.processor.XImageProcessor;

/**
 * 图片加载器接口。
 * 包含了图片加载器的最基本操作。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 下午3:50
 * To change this template use File | Settings | File Templates.
 */
public interface XImageLoader {

    /**
     * 初始化函数,初始化一些默认的图片资源。
     * 第一次创建图片加载器时必须调用此方法
     * @param emptyImageResource
     * @param defaultImageResource
     * @param loadingImageResource
     * @param errorImageResource
     */
    void init(Context context,
              int emptyImageResource,
              int defaultImageResource,
              int loadingImageResource,
              int errorImageResource);

    /**
     * 清空所有图片缓存池
     */
    void clearImageCache();

    /**
     * 加载真正的图片(并缓存到内存中) 。
     * 一般在异步线程中执行
     * @param context
     * @param imageUrl
     * @param size
     * @return
     */
    Bitmap loadRealImage(Context context, String imageUrl,
                         XImageProcessor.ImageSize size);

    /**
     * 根据图片的远程url获取本地下载保存的图片文件地址。
     * @param imgUrl 图片的远程url
     * @return 如果存在，返回文件名；不存在则返回null
     */
    String getLocalImage(String imgUrl);
}
