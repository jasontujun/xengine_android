package com.xengine.android.media.image.loader.cache;

import android.graphics.Bitmap;
import com.xengine.android.media.image.processor.XImageProcessor;

/**
 * 图片缓存池的接口
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 下午2:36
 * To change this template use File | Settings | File Templates.
 */
public interface XImageCache {

    /**
     * 获取缓存中的Bitmap
     * @param imageUrl
     * @param size
     * @return 如果缓存中存在对应url的bitmap，则返回；否则返回null
     */
    Bitmap getCacheBitmap(String imageUrl, XImageProcessor.ImageSize size);

    /**
     * 缓存加载的Bitmap
     * @param imageUrl
     * @param bmp
     * @param size
     * @return 缓存成功则返回true；否则妇女会false
     */
    boolean saveCacheBitmap(String imageUrl, Bitmap bmp, XImageProcessor.ImageSize size);

    /**
     * 清空所有图片缓存池
     */
    void clearImageCache();

    /**
     * 对于同一tag下的bitmap, 保持当前传入bitmap在内存中不被销毁，
     * 而之前保持的bitmap将根据情况被销毁。
     * 主要用于ImageSwitcher之类的控件加载图片的需要。
     * WARNING：谨慎使用该方法，有可能因为保持过多的bitmap而导致抛出OutOfMemoryException!
     * @see com.xengine.android.media.image.loader.XImageSwitcherLocalLoader
     * @param tag 标识不同的Bitmap
     * @param bmp
     * @param size
     */
    void addKeepingBitmap(String tag, Bitmap bmp, XImageProcessor.ImageSize size);

    /**
     * 清空所有额外保持的bitmap。
     */
    void clearKeepingBitmap();
}
