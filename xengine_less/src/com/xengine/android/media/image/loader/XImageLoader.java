package com.xengine.android.media.image.loader;

import android.content.Context;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import com.xengine.android.media.image.processor.XImageProcessor;

/**
 * 内存加载图片的接口
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午7:11
 * To change this template use File | Settings | File Templates.
 */
public interface XImageLoader {

    /**
     * 初始化函数。初始化一些默认的图片资源。
     * @param emptyImageResource
     * @param defaultImageResource
     * @param loadingImageResource
     * @param errorImageResource
     */
    void init(int emptyImageResource,
              int defaultImageResource,
              int loadingImageResource,
              int errorImageResource);

    /**
     * 清空所有图片缓存池
     */
    void clearImageCache();

    /**
     * 异步加载图片(对ImageView)
     * @param context
     * @param imageUrl
     * @param imageView
     * @param size
     */
    void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView, XImageProcessor.ImageSize size);

    /**
     * 异步加载图片(对ImageSwitcher)
     * @param context
     * @param imageUrl
     * @param imageSwitcher
     * @param size
     */
    void asyncLoadBitmap(Context context, String imageUrl,
                    ImageSwitcher imageSwitcher, XImageProcessor.ImageSize size);

    /**
     * 同步加载图片(对ImageView)
     * @param context
     * @param imageUrl
     * @param imageView
     * @param size
     */
    public void syncLoadBitmap(Context context, String imageUrl,
                               ImageView imageView, XImageProcessor.ImageSize size);

    /**
     * 设置显示图片时候的渐变效果
     * @param fading
     */
    void setFading(boolean fading);
}
