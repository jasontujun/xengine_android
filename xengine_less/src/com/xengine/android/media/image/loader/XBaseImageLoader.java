package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.animation.AlphaAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import com.xengine.android.media.image.processor.XAndroidImageProcessor;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.utils.XStringUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 图片加载器。
 * 二级缓存（内存 + Sd卡的图片缓存）。
 * 异步方式加载。
 * 同步方式加载。
 * Created by jasontujun.
 * Date: 12-10-9
 * Time: 下午1:22
 */
public abstract class XBaseImageLoader implements XImageLoader {

    private Map<String, SoftReference<Bitmap>> mSmallImageCache;// 小图缓存
    private Map<String, SoftReference<Bitmap>> mScreenImageCache;// 屏幕图缓存
    private Map<String, SoftReference<Bitmap>> mOriginalImageCache;// 原始图缓存
    private boolean isFading;// 标识是否开启渐变效果
    private int mEmptyImageResource;
    private int mDefaultImageResource;
    private int mLoadingImageResource;
    private int mErrorImageResource;

    public XBaseImageLoader() {
        mSmallImageCache = new HashMap<String, SoftReference<Bitmap>>();
        mScreenImageCache = new HashMap<String, SoftReference<Bitmap>>();
        mOriginalImageCache = new HashMap<String, SoftReference<Bitmap>>();
        isFading = true;
    }

    /**
     * 根据图片的远程url获取本地下载保存的图片文件地址。
     * @param imgUrl 图片的远程url
     * @return 如果存在，返回文件名；不存在则返回null
     */
    public abstract String getLocalImage(String imgUrl);

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
    public void setFading(boolean fading) {
        this.isFading = fading;
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

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView, XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一张图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 如果是默认不存在的图标提示（“点击加载”，“加载中”，“加载失败”等），则不需要异步
            bitmap = loadErrorImage(context, imageUrl);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);// TIP 不要渐变效果
                return;
            }

            // 如果是真正图片，则需要异步加载
            Resources resources = context.getResources();
            Bitmap mPlaceHolderBitmap = BitmapFactory.
                    decodeResource(resources, mEmptyImageResource);// 占位图片
            final AsyncImageTask task = new AsyncImageTask(context, imageView, imageUrl, size);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(null);
        }
    }


    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageSwitcher imageSwitcher, XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
            imageSwitcher.setTag(null);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一张图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageSwitcher)) {
            // 如果是默认不存在的图标提示（“点击加载”，“加载中”，“加载失败”等），则不需要异步
            bitmap = loadErrorImage(context, imageUrl);
            if(bitmap != null) {
                imageSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
                imageSwitcher.setTag(null);
                return;
            }

            // 如果是真正图片，则需要异步加载
            final AsyncImageTask2 task = new AsyncImageTask2(context, imageSwitcher, imageUrl, size);
            imageSwitcher.setTag(task);
            task.execute(null);
        }
    }

    @Override
    public void syncLoadBitmap(Context context, String imageUrl,
                               ImageView imageView, XImageProcessor.ImageSize size) {

        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 检测本地图片是否对应为图标提示（“点击加载”，“加载中”，“加载失败”等）
        bitmap = loadErrorImage(context, imageUrl);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);// TIP 不要渐变效果
            return;
        }

        // 如果是真正图片，则需要异步加载
        bitmap = loadRealImage(context, imageUrl, size);
        imageView.setImageBitmap(bitmap);
        showViewAnimation(context, imageView);
    }


    /**
     * 获取缓存中的Bitmap
     * @param imageUrl
     * @param size
     * @return
     */
    private Bitmap getCacheBitmap(String imageUrl, XImageProcessor.ImageSize size) {
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

    private void saveCacheBitmap(String imageUrl, Bitmap bmp, XImageProcessor.ImageSize size) {
        if(bmp == null || XStringUtil.isNullOrEmpty(imageUrl)) {
            return;
        }
        switch (size) {
            case SMALL:
                mSmallImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                break;
            case SCREEN:
                mScreenImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                break;
            case ORIGIN:
                mOriginalImageCache.put(imageUrl, new SoftReference<Bitmap>(bmp));
                break;
        }
    }

    /**
     * 判断imgUrl对应的本地图片是否存在。
     * 若不存在，返回true。若真正存在，返回false
     * @param context
     * @param imageUrl
     * @return
     */
    private Bitmap loadErrorImage(Context context, String imageUrl) {
        Resources resources = context.getResources();
        String localImageFile = getLocalImage(imageUrl);
        if(XStringUtil.isNullOrEmpty(localImageFile)) {
            // 返回缺省图片（图片不存在）
            return BitmapFactory.decodeResource(resources, mDefaultImageResource);
        }
        if(localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
            // 返回错误图片（图片错误）
            return BitmapFactory.decodeResource(resources, mErrorImageResource);
        }
        if(localImageFile.equals(XImageLocalUrl.IMG_LOADING)) {
            // 返回加载提示的图片（图片加载中）
            return BitmapFactory.decodeResource(resources, mLoadingImageResource);
        }

        return null;
    }

    /**
     * 加载真正的图片(并缓存到内存中)
     * @param imageUrl
     * @param size
     * @return
     */
    private Bitmap loadRealImage(Context context, String imageUrl, XImageProcessor.ImageSize size) {
        try {
            String localImageFile = getLocalImage(imageUrl);
            Bitmap bmp = XAndroidImageProcessor.getInstance()
                    .getLocalImage(localImageFile, size);// 加载size尺寸大小的图片
            if(bmp != null) {
                saveCacheBitmap(imageUrl, bmp, size);// 缓存图片
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
     * 取消该组件上之前的异步加载任务
     * @param imageUrl
     * @param imageView
     * @return
     */
    private static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final AsyncImageTask asyncImageTask = getAsyncImageTask(imageView);
        if (asyncImageTask != null) {
            final String url = asyncImageTask.getImageUrl();
            if (url == null || !url.equals(imageUrl)) {
                // Cancel previous task
                asyncImageTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * 获取该组件上的异步加载任务
     * @param imageView
     * @return
     */
    private static AsyncImageTask getAsyncImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private void showViewAnimation(Context context, ImageView imageView) {
        if(isFading) {
            AlphaAnimation fadeAnimation = new AlphaAnimation(0 ,1);
            fadeAnimation.setDuration(context.getResources().
                    getInteger(android.R.integer.config_shortAnimTime));
            imageView.startAnimation(fadeAnimation);
        }
    }


    /**
     * 异步加载图片(用于ImageView)
     */
    private class AsyncImageTask extends AsyncTask<Void, Void, Bitmap> {
        private Context context;
        private final WeakReference<ImageView> imageViewReference;
        private String imageUrl;
        private XImageProcessor.ImageSize size;// 加载的图片尺寸

        public AsyncImageTask(Context context, ImageView imageView, String imageUrl,
                              XImageProcessor.ImageSize size) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.context = context;
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.imageUrl = imageUrl;
            this.size = size;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            return loadRealImage(context, imageUrl, size);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final AsyncImageTask asyncImageTask = getAsyncImageTask(imageView);
                if (this == asyncImageTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    showViewAnimation(context, imageView);
                }
            }
        }
    }

    /**
     * 含AysncTask的BitmapDrawable
     */
    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<AsyncImageTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, AsyncImageTask asyncImageTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<AsyncImageTask>(asyncImageTask);
        }

        public AsyncImageTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * 取消该组件上之前的异步加载任务
     * @param imageUrl
     * @param imageSwitcher
     * @return
     */
    private static boolean cancelPotentialWork(String imageUrl,
                                               ImageSwitcher imageSwitcher) {
        final AsyncImageTask2 asyncImageTask = getAsyncImageTask(imageSwitcher);
        if (asyncImageTask != null) {
            final String url = asyncImageTask.getImageUrl();
            if (url == null || !url.equals(imageUrl)) {
                // Cancel previous task
                asyncImageTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * 获取该组件上的异步加载任务
     * @param imageSwitcher
     * @return
     */
    private static AsyncImageTask2 getAsyncImageTask(ImageSwitcher imageSwitcher) {
        if (imageSwitcher != null) {
            Object tag = imageSwitcher.getTag();
            if (tag != null && tag instanceof AsyncImageTask2)
                return (AsyncImageTask2) tag;
        }
        return null;
    }


    /**
     * 异步加载图片(用于ImageSwitcher)
     */
    private class AsyncImageTask2 extends AsyncTask<Void, Void, Bitmap> {
        private Context context;
        private final WeakReference<ImageSwitcher> switcherReference;
        private String imageUrl;
        private XImageProcessor.ImageSize size;// 加载的图片尺寸

        public AsyncImageTask2(Context context, ImageSwitcher switcher, String imageUrl,
                               XImageProcessor.ImageSize size) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.context = context;
            this.switcherReference = new WeakReference<ImageSwitcher>(switcher);
            this.imageUrl = imageUrl;
            this.size = size;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            return loadRealImage(context, imageUrl, size);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (switcherReference != null && bitmap != null) {
                final ImageSwitcher switcher = switcherReference.get();
                final AsyncImageTask2 asyncImageTask = getAsyncImageTask(switcher);
                if (this == asyncImageTask && switcher != null) {
                    switcher.setImageDrawable(new BitmapDrawable(bitmap));
                    switcher.setTag(null);
                }
            }
        }
    }
}
