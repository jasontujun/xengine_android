package com.xengine.android.media.image.loader.cache;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.xengine.android.media.image.processor.XImageProcessor;

import java.lang.ref.ReferenceQueue;
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
public final class XSimpleImageCache implements XImageCache {

    private static XSimpleImageCache instance;

    public static synchronized XSimpleImageCache getInstance() {
        if (instance == null) {
            instance = new XSimpleImageCache();
        }
        return instance;
    }

    private XSimpleImageCache(){
        mSmallImageCache = new HashMap<String, BitmapRef>();
        mScreenImageCache = new HashMap<String, BitmapRef>();
        mOriginalImageCache = new HashMap<String, BitmapRef>();
        mReferenceQueue = new ReferenceQueue<Bitmap>();
    }

    private Map<String, BitmapRef> mSmallImageCache;// 小图缓存
    private Map<String, BitmapRef> mScreenImageCache;// 屏幕图缓存
    private Map<String, BitmapRef> mOriginalImageCache;// 原始图缓存
    private ReferenceQueue<Bitmap> mReferenceQueue;// 垃圾reference队列

    /**
     * 继承SoftReference，使得每一个实例都具有可识别的标识。
     * 并且该标识与其在HashMap内的key相同。
     */
    private class BitmapRef extends SoftReference<Bitmap> {
        private String key;
        private XImageProcessor.ImageSize size;

        public BitmapRef(Bitmap bmp, ReferenceQueue<Bitmap> q,
                         String url, XImageProcessor.ImageSize s) {
            super(bmp, q);
            key = url;
            size = s;
        }
    }

    @Override
    public Bitmap getCacheBitmap(String imageUrl, XImageProcessor.ImageSize size) {
        clearRubbishReference();
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

        clearRubbishReference();
        switch (size) {
            case SMALL:
                mSmallImageCache.put(imageUrl, new BitmapRef(bmp, mReferenceQueue, imageUrl, size));
                return true;
            case SCREEN:
                mScreenImageCache.put(imageUrl, new BitmapRef(bmp, mReferenceQueue, imageUrl, size));
                return true;
            case ORIGIN:
                mOriginalImageCache.put(imageUrl, new BitmapRef(bmp, mReferenceQueue, imageUrl, size));
                return true;
        }
        return false;
    }

    /**
     * 清理被gc回收掉后，遗留在map中的垃圾引用
     */
    private void clearRubbishReference() {
        BitmapRef ref = null;
        while ((ref = (BitmapRef) mReferenceQueue.poll()) != null) {
            switch (ref.size) {
                case SMALL:
                    mSmallImageCache.remove(ref.key);
                    break;
                case SCREEN:
                    mScreenImageCache.remove(ref.key);
                    break;
                case ORIGIN:
                    mOriginalImageCache.remove(ref.key);
                    break;
            }
        }
    }

    @Override
    public void clearImageCache() {
        clearRubbishReference();
        clearImageCache(mSmallImageCache);
        clearImageCache(mScreenImageCache);
        clearImageCache(mOriginalImageCache);
    }

    private void clearImageCache(Map<String, BitmapRef> cache) {
        Iterator<BitmapRef> iterator = cache.values().iterator();
        while (iterator.hasNext()) {
            BitmapRef softReference = iterator.next();
            Bitmap bitmap = softReference.get();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        cache.clear();
    }

}
