package com.xengine.android.media.image.loader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.utils.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过LruCache实现的图片缓存池.
 * 用单例模式，统一管理所有在内存中的图片缓存。
 * 给定缓存的大小，利用LRU原则（最久未被使用的图片会被优先回收）管理缓存中的对象。
 * @see android.support.v4.util.LruCache
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-22
 * Time: 上午11:53
 * To change this template use File | Settings | File Templates.
 */
public final class XLruImageCache implements XImageCache {
    private static final String TAG = XLruImageCache.class.getSimpleName();

    private static XLruImageCache instance;

    public static synchronized XLruImageCache getInstance() {
        if (instance == null) {
            instance = new XLruImageCache();
        }
        return instance;
    }

    private XLruImageCache(){
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        XLog.d(TAG, "XLruImageCache size: " + cacheSize + "KB");

        mSmallImageCache = new XLruCache(cacheSize, "SmallCache");
        mScreenImageCache = new XLruCache((int) (1.5 * cacheSize), "ScreenCache");
        mOriginalImageCache = new XLruCache(cacheSize, "OriginalCache");
    }

    private XLruCache mSmallImageCache;// 小图缓存
    private XLruCache mScreenImageCache;// 屏幕图缓存
    private XLruCache mOriginalImageCache;// 原始图缓存

    @Override
    public Bitmap getCacheBitmap(String imageUrl, XImageProcessor.ImageSize size) {
        Bitmap bitmap = null;
        switch (size) {
            case SMALL:
                bitmap = mSmallImageCache.get(imageUrl);
                break;
            case SCREEN:
                bitmap = mScreenImageCache.get(imageUrl);
                break;
            case ORIGIN:
                bitmap = mOriginalImageCache.get(imageUrl);
                break;
        }
        return bitmap;
    }

    @Override
    public boolean saveCacheBitmap(String imageUrl, Bitmap bmp, XImageProcessor.ImageSize size) {
        if (bmp == null || TextUtils.isEmpty(imageUrl))
            return false;

        XLog.d(TAG, size + " saveCacheBitmap. bmp:" + bmp);
        switch (size) {
            case SMALL:
                if (getCacheBitmap(imageUrl, size) == null) {
                    XLog.d(TAG, "success!! " + size + " saveCacheBitmap. bmp:" + bmp);
                    mSmallImageCache.put(imageUrl, bmp);
                }
                return true;
            case SCREEN:
                if (getCacheBitmap(imageUrl, size) == null) {
                    XLog.d(TAG, "success!! " + size + " saveCacheBitmap. bmp:" + bmp);
                    mScreenImageCache.put(imageUrl, bmp);
                }
                return true;
            case ORIGIN:
                if (getCacheBitmap(imageUrl, size) == null) {
                    XLog.d(TAG, "success!! " + size + " saveCacheBitmap. bmp:" + bmp);
                    mOriginalImageCache.put(imageUrl, bmp);
                }
                return true;
        }
        return false;
    }

    @Override
    public void clearImageCache() {
        mSmallImageCache.evictAll();
        mScreenImageCache.evictAll();
        mOriginalImageCache.evictAll();
    }

    @Override
    public void addKeepingBitmap(String tag, Bitmap bmp, XImageProcessor.ImageSize size) {;
        switch (size) {
            case SMALL:
                mSmallImageCache.addKeepingBitmap(tag, bmp);
                break;
            case SCREEN:
                mScreenImageCache.addKeepingBitmap(tag, bmp);
                break;
            case ORIGIN:
                mOriginalImageCache.addKeepingBitmap(tag, bmp);
                break;
        }
    }

    @Override
    public void clearKeepingBitmap() {
        mSmallImageCache.clearKeepingBitmap();
        mScreenImageCache.clearKeepingBitmap();
        mOriginalImageCache.clearKeepingBitmap();
    }


    /**
     * 继承自LruCache的自定义缓存类。
     */
    private class XLruCache extends LruCache<String, Bitmap> {

        private String mName;
        private Map<String, KeepBitmap> mKeepBitmaps;

        private class KeepBitmap {
            private Bitmap bitmap;// 持有的bitmap
            private boolean abandon;// 持有的bitmap是否被销毁

            private KeepBitmap(Bitmap bitmap, boolean abandon) {
                this.bitmap = bitmap;
                this.abandon = abandon;
            }
        }

        public XLruCache(int maxSize, String name) {
            super(maxSize);
            mName = name;
            mKeepBitmaps = new HashMap<String, KeepBitmap>();
        }

        public void addKeepingBitmap(String tag, Bitmap bitmap) {
            XLog.d(TAG, mName + " addKeepingBitmap. bitmap:" + bitmap
                    + ",addKeepingBitmap:" + mKeepBitmaps);

            if (mKeepBitmaps.containsKey(tag)) {
                KeepBitmap keepBitmap = mKeepBitmaps.get(tag);
                // 如果同一个bitmap已经持有，则返回
                if (keepBitmap.bitmap == bitmap)
                    return;
                // 根据abandon，销毁持有的bitmap
                if (keepBitmap.bitmap != null && !keepBitmap.bitmap.isRecycled()
                        && keepBitmap.abandon) {
                    XLog.d(TAG, mName + ". recycle keeping bitmap :" + keepBitmap.bitmap);
                    keepBitmap.bitmap.recycle();
                }
            }
            mKeepBitmaps.put(tag, new KeepBitmap(bitmap, false));
        }

        public void clearKeepingBitmap() {
            for (Map.Entry<String, KeepBitmap> entry : mKeepBitmaps.entrySet()) {
                KeepBitmap keepBitmap = entry.getValue();
                if (keepBitmap.bitmap != null && !keepBitmap.bitmap.isRecycled()
                        && keepBitmap.abandon)
                    keepBitmap.bitmap.recycle();
            }
            mKeepBitmaps.clear();
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            final int byteCount = bitmap.getRowBytes() * bitmap.getHeight();
            XLog.d(TAG, mName + " sizeOf. bitmap:" + bitmap
                    + ",size:" + (byteCount / 1024) + "KB");
            return byteCount / 1024;
        }

        @Override
        protected void entryRemoved(boolean evicted, String key,
                                    Bitmap oldValue, Bitmap newValue) {
            XLog.d(TAG, mName + " entryRemoved. oldBitmap:" + oldValue
                    + ",addKeepingBitmap:" + mKeepBitmaps);

            if (oldValue != null && !oldValue.isRecycled()) {
                // 判断是否是要保持的bitmap
                for (Map.Entry<String, KeepBitmap> entry : mKeepBitmaps.entrySet()) {
                    KeepBitmap keepBitmap = entry.getValue();
                    if (keepBitmap.bitmap  == oldValue) {
                        XLog.d(TAG, mName + " entryRemoved. !!!hit mKeepBitmap!!!");
                        keepBitmap.abandon = true;
                        return;
                    }
                }
                // 如果不是，就销毁
                XLog.d(TAG, mName + " entryRemoved. !!!Recycle!!!");
                oldValue.recycle();
            }
        }
    }
}
