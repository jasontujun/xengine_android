package com.xengine.android.media.image.loader.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import com.xengine.android.media.image.processor.XImageProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-22
 * Time: 上午11:53
 * To change this template use File | Settings | File Templates.
 */
public class XImageLruCache implements XImageCache {

    private static XImageLruCache instance;

    public static synchronized XImageLruCache getInstance() {
        if (instance == null) {
            instance = new XImageLruCache();
        }
        return instance;
    }

    private XImageLruCache(){
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mSmallImageCache = new XLruCache(cacheSize);
        mScreenImageCache = new XLruCache(cacheSize);
        mOriginalImageCache = new XLruCache(cacheSize);
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

        switch (size) {
            case SMALL:
                if (getCacheBitmap(imageUrl, size) == null)
                    mSmallImageCache.put(imageUrl, bmp);
                return true;
            case SCREEN:
                if (getCacheBitmap(imageUrl, size) == null)
                    mScreenImageCache.put(imageUrl, bmp);
                return true;
            case ORIGIN:
                if (getCacheBitmap(imageUrl, size) == null)
                    mOriginalImageCache.put(imageUrl, bmp);
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

    private class XLruCache extends LruCache<String, Bitmap> {

        public XLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            final int byteCount = bitmap.getRowBytes() * bitmap.getHeight();
            return byteCount / 1024;
        }

        protected void entryRemoved(boolean evicted, String key,
                                    Bitmap oldValue, Bitmap newValue) {
            if (oldValue != null)
                oldValue.recycle();
        }
    }
}
