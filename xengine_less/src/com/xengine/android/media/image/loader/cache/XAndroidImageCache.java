package com.xengine.android.media.image.loader.cache;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.xengine.android.media.image.processor.XImageProcessor;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 图片缓存池.
 * 统一管理所有在内存中的图片缓存,所以用单例模式。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 下午2:39
 * To change this template use File | Settings | File Templates.
 */
public final class XAndroidImageCache implements XImageCache {

    private static XAndroidImageCache instance;

    public static synchronized XAndroidImageCache getInstance() {
        if (instance == null) {
            instance = new XAndroidImageCache();
        }
        return instance;
    }

    private XAndroidImageCache(){
        mSmallImageCache = new HashMap<String, SoftReference<Bitmap>>();
        mScreenImageCache = new HashMap<String, SoftReference<Bitmap>>();
        mOriginalImageCache = new HashMap<String, SoftReference<Bitmap>>();
    }

    private Map<String, SoftReference<Bitmap>> mSmallImageCache;// 小图缓存
    private Map<String, SoftReference<Bitmap>> mScreenImageCache;// 屏幕图缓存
    private Map<String, SoftReference<Bitmap>> mOriginalImageCache;// 原始图缓存

    @Override
    public Bitmap getCacheBitmap(String imageUrl, XImageProcessor.ImageSize size) {
        Bitmap bitmap = null;
        switch (size) {
            case SMALL:
                if (mSmallImageCache.containsKey(imageUrl))
                    bitmap = mSmallImageCache.get(imageUrl).get();
                break;
            case SCREEN:
                if (mScreenImageCache.containsKey(imageUrl))
                    bitmap = mScreenImageCache.get(imageUrl).get();
                break;
            case ORIGIN:
                if (mOriginalImageCache.containsKey(imageUrl))
                    bitmap = mOriginalImageCache.get(imageUrl).get();
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
                mSmallImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                return true;
            case SCREEN:
                mScreenImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                return true;
            case ORIGIN:
                mOriginalImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                return true;
        }
        return false;
    }

    @Override
    public void clearImageCache() {
        clearImageCache(mSmallImageCache);
        clearImageCache(mScreenImageCache);
        clearImageCache(mOriginalImageCache);
    }

    private void clearImageCache(Map<String, SoftReference<Bitmap>> cache) {
        Iterator<SoftReference<Bitmap>> iterator = cache.values().iterator();
        while (iterator.hasNext()) {
            SoftReference<Bitmap> softReference = iterator.next();
            Bitmap bitmap = softReference.get();
            if(bitmap != null) {
                bitmap.recycle();
            }
        }
        cache.clear();
    }

}
