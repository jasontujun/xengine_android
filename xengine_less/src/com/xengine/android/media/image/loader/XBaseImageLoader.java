package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import com.xengine.android.media.image.loader.cache.XImageCache;
import com.xengine.android.media.image.loader.cache.XLruImageCache;
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
        mImageCache = XLruImageCache.getInstance();// 使用LruCache
    }

    @Override
    public void init(Context context,
                     int emptyImageResource,
                     int defaultImageResource,
                     int loadingImageResource,
                     int errorImageResource) {
        mEmptyImageResource = emptyImageResource;
        mDefaultImageResource = defaultImageResource;
        mLoadingImageResource = loadingImageResource;
        mErrorImageResource = errorImageResource;
        // 保存默认的资源图片进缓存中
        Resources resources = context.getResources();
        mImageCache.saveCacheBitmap(XImageLocalUrl.IMG_EMPTY,
                BitmapFactory.decodeResource(resources, mEmptyImageResource),
                XImageProcessor.ImageSize.ORIGIN);// 占位图片
        mImageCache.saveCacheBitmap(XImageLocalUrl.IMG_DEFAULT,
                BitmapFactory.decodeResource(resources, mDefaultImageResource),
                XImageProcessor.ImageSize.ORIGIN);// 缺省图片
        mImageCache.saveCacheBitmap(XImageLocalUrl.IMG_LOADING,
                BitmapFactory.decodeResource(resources, mLoadingImageResource),
                XImageProcessor.ImageSize.ORIGIN);// 加载中图片
        mImageCache.saveCacheBitmap(XImageLocalUrl.IMG_ERROR,
                BitmapFactory.decodeResource(resources, mErrorImageResource),
                XImageProcessor.ImageSize.ORIGIN);// 错误图片
    }

    @Override
    public void clearImageCache() {
        mImageCache.clearImageCache();
    }

    @Override
    public Bitmap loadRealImage(Context context, String imageUrl, XImageProcessor.ImageSize size) {
        try {
            String localImageFile = getLocalImage(imageUrl);
            Bitmap bmp = XAndroidImageProcessor.getInstance()
                    .getLocalImage(localImageFile, size);// 加载对应尺寸的图片
            if (bmp != null) {
                mImageCache.saveCacheBitmap(imageUrl, bmp, size);// 缓存图片
                return bmp;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 返回错误图片（图片不存在）
        return getImageResource(context, XImageLocalUrl.IMG_ERROR);
    }

    /**
     * 获取默认的资源图片（缺省、占位、加载中、加载错误）
     * @param context
     * @return
     */
    protected Bitmap getImageResource(Context context, String localUrl) {
        Bitmap bitmap = mImageCache.getCacheBitmap(localUrl,
                XImageProcessor.ImageSize.ORIGIN);
        if (bitmap != null && !bitmap.isRecycled())
            return bitmap;

        if (XImageLocalUrl.IMG_DEFAULT.equals(localUrl))
            bitmap = BitmapFactory.decodeResource(context.getResources(), mDefaultImageResource);
        else if (XImageLocalUrl.IMG_EMPTY.equals(localUrl))
            bitmap = BitmapFactory.decodeResource(context.getResources(), mEmptyImageResource);
        else if (XImageLocalUrl.IMG_LOADING.equals(localUrl))
            bitmap = BitmapFactory.decodeResource(context.getResources(), mLoadingImageResource);
        else if (XImageLocalUrl.IMG_ERROR.equals(localUrl))
            bitmap = BitmapFactory.decodeResource(context.getResources(), mErrorImageResource);
        else
            return null;
        mImageCache.saveCacheBitmap(localUrl,
                bitmap,
                XImageProcessor.ImageSize.ORIGIN);// 占位图片
        return bitmap;
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
