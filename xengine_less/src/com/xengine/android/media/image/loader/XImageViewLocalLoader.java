package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.lang.ref.WeakReference;

/**
 * 本地图片加载器（用于ImageView）。
 * 特点：
 * 1. 只负责本地加载，不涉及下载.
 * 2. 二级缓存（内存 + sd卡的图片缓存）。
 * 3. 异步方式加载。
 * 4. 同步方式加载。
 * 5. 多线程并发，无序且比较耗费资源
 * @see XImageSwitcherLocalLoader 用于ImageSwitcher的本地图片加载器
 * Created by jasontujun.
 * Date: 12-10-9
 * Time: 下午1:22
 */
public abstract class XImageViewLocalLoader extends XBaseImageLoader
        implements XImageLocalLoader<ImageView> {
    private static final String TAG = XImageViewLocalLoader.class.getSimpleName();

    protected boolean mIsFading;// 标识是否开启渐变效果

    public XImageViewLocalLoader() {
        super();
        mIsFading = true;
    }
    public void setFadingLoad(boolean fading) {
        this.mIsFading = fading;
    }

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView, XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一个ImageView但不同图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 如果是默认不存在的图标提示（“缺省图片”，“加载中”，“加载失败”等），则不需要异步
            if (loadErrorImage(imageUrl, imageView)) // TIP 不要渐变效果
                return;

            // 如果是真正图片，则需要异步加载
            Resources resources = context.getResources();
            Bitmap mPlaceHolderBitmap = BitmapFactory.
                    decodeResource(resources, mEmptyImageResource);// 占位图片
            final LocalImageViewAsyncTask task = new LocalImageViewAsyncTask(context, imageView, imageUrl, size);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(null);
        }
    }

    @Override
    public void syncLoadBitmap(Context context, String imageUrl,
                               ImageView imageView, XImageProcessor.ImageSize size) {

        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 检测本地图片是否对应为图标提示（“缺省图片”，“加载中”，“加载失败”等）
        if (loadErrorImage(imageUrl, imageView)) // TIP 不要渐变效果
            return;

        // 如果是真正图片，则需要异步加载
        bitmap = loadRealImage(context, imageUrl, size);
        imageView.setImageBitmap(bitmap);
        showViewAnimation(context, imageView);
    }

    /**
     * 判断imgUrl对应的本地图片是否存在。
     * 若不存在，返回true。若真正存在，返回false
     * @param imageUrl
     * @return
     */
    protected boolean loadErrorImage(String imageUrl, ImageView imageView) {
        String localImageFile = getLocalImage(imageUrl);
        if (XStringUtil.isNullOrEmpty(localImageFile)) {
            // 返回缺省图片（图片不存在）
            imageView.setImageResource(mDefaultImageResource);
            return true;
        }
        if (localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
            // 返回错误图片（图片错误）
            imageView.setImageResource(mErrorImageResource);
            return true;
        }
        if (localImageFile.equals(XImageLocalUrl.IMG_LOADING)) {
            // 返回加载提示的图片（图片加载中）
            imageView.setImageResource(mLoadingImageResource);
            return true;
        }
        return false;
    }

    /**
     * 取消该组件上之前的异步加载任务(用于ImageView)
     * @param imageUrl
     * @param imageView
     * @return
     */
    protected static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final LocalImageViewAsyncTask localImageViewAsyncTask = getAsyncImageTask(imageView);
        if (localImageViewAsyncTask != null) {
            final String url = localImageViewAsyncTask.getImageUrl();
            if (url == null || !url.equals(imageUrl)) {
                // Cancel previous task
                localImageViewAsyncTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * 获取该组件上的异步加载任务(用于ImageView)
     * @param imageView
     * @return
     */
    protected static LocalImageViewAsyncTask getAsyncImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                AsyncTask task = asyncDrawable.getBitmapWorkerTask();
                if (task instanceof LocalImageViewAsyncTask)
                    return (LocalImageViewAsyncTask) task;
            }
        }
        return null;
    }

    protected void showViewAnimation(Context context, ImageView imageView) {
        if (mIsFading) {
            AlphaAnimation fadeAnimation = new AlphaAnimation(0 ,1);
            fadeAnimation.setDuration(context.getResources().
                    getInteger(android.R.integer.config_shortAnimTime));
            imageView.startAnimation(fadeAnimation);
        }
    }

    /**
     * 异步本地加载图片的AsyncTask(用于ImageView)
     */
    public class LocalImageViewAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private Context context;
        private final WeakReference<ImageView> imageViewReference;
        private String imageUrl;
        private XImageProcessor.ImageSize size;// 加载的图片尺寸

        public LocalImageViewAsyncTask(Context context, ImageView imageView, String imageUrl,
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
            XLog.d(TAG, "LocalImageViewAsyncTask doInBackground(). url:" + imageUrl);
            return loadRealImage(context, imageUrl, size);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            XLog.d(TAG, "LocalImageViewAsyncTask onPostExecute(). url:" + imageUrl);
            if (isCancelled())
                bitmap = null;

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final LocalImageViewAsyncTask localImageViewAsyncTask = getAsyncImageTask(imageView);
                if (this == localImageViewAsyncTask && imageView != null) {
                    XLog.d(TAG, "set real image. url:" + imageUrl);
                    imageView.setImageBitmap(bitmap);
                    showViewAnimation(context, imageView);
                }
            }
        }
    }

}
