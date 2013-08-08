package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import com.xengine.android.media.image.loader.cache.XAndroidImageCache;
import com.xengine.android.media.image.loader.cache.XImageCache;
import com.xengine.android.media.image.processor.XAndroidImageProcessor;
import com.xengine.android.media.image.processor.XImageProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 实现图片加载器接口的基础类。
 * @see XImageViewLocalLoader
 * @see XImageSwitcherLocalLoader
 * @see XImageViewRemoteLoader
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 下午3:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class XBaseImageLoader implements XImageLoader{

    protected XImageCache mImageCache;// 图片缓存池
    protected int mEmptyImageResource;
    protected int mDefaultImageResource;
    protected int mLoadingImageResource;
    protected int mErrorImageResource;

    public XBaseImageLoader() {
        mImageCache = XAndroidImageCache.getInstance();
    }

    public void init(int emptyImageResource,
                     int defaultImageResource,
                     int loadingImageResource,
                     int errorImageResource) {
        mEmptyImageResource = emptyImageResource;
        mDefaultImageResource = defaultImageResource;
        mLoadingImageResource = loadingImageResource;
        mErrorImageResource = errorImageResource;
    }

    @Override
    public void clearImageCache() {
        mImageCache.clearImageCache();
    }

    /**
     * 加载真正的图片(并缓存到内存中)
     * @param imageUrl
     * @param size
     * @return
     */
    public Bitmap loadRealImage(Context context, String imageUrl, XImageProcessor.ImageSize size) {
        try {
            String localImageFile = getLocalImage(imageUrl);
            Bitmap bmp = XAndroidImageProcessor.getInstance()
                    .getLocalImage(localImageFile, size);// 加载size尺寸大小的图片
            if(bmp != null) {
                mImageCache.saveCacheBitmap(imageUrl, bmp, size);// 缓存图片
                return bmp;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回错误图片（图片不存在）
        Resources resources = context.getResources();
        return BitmapFactory.decodeResource(resources, mErrorImageResource);
    }

    /**
     * 含AysncTask的BitmapDrawable
     */
    protected class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<AsyncTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, AsyncTask asyncImageViewTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<AsyncTask>(asyncImageViewTask);
        }

        public AsyncTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
