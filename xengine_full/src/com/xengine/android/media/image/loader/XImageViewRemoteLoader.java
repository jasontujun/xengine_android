package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.system.download.XSerialDownloadListener;
import com.xengine.android.utils.XLog;

import java.lang.ref.WeakReference;

/**
 * 远程图片加载器。
 * 特点：
 * 1. 包括本地加载，如果本地没有则下载。
 * 2. 加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * 3. 异步方式加载。
 * 4. 多线程并发加载，无序且比较耗费资源。
 * @see com.xengine.android.media.image.loader.XScrollRemoteLoader 待滑动延迟的远程图片加载器。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XImageViewRemoteLoader extends XBaseImageLoader
        implements XImageRemoteLoader<ImageView> {
    private static final String TAG = XImageViewRemoteLoader.class.getSimpleName();

    protected XImageDownload mImageDownloadMgr;// 图片下载管理器
    protected boolean mIsFading;// 标识是否开启渐变效果

    public XImageViewRemoteLoader(XImageDownload imageDownloadMgr) {
        super();
        mIsFading = true;
        mImageDownloadMgr = imageDownloadMgr;
    }

    public void setFadingLoad(boolean fading) {
        this.mIsFading = fading;
    }

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView,
                                XImageProcessor.ImageSize size,
                                XSerialDownloadListener listener) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null && !bitmap.isRecycled()) {
            cancelPotentialWork(imageUrl, imageView);
            imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则将imageView设置为对应状态的图标，准备启动异步线程
        // （先取消之前可能对同一个ImageView但不同图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 如果local_image标记为“加载中”，即图片正在下载，什么都不做
            String localImageFile = getLocalImage(imageUrl);
            if (XImageLocalUrl.IMG_LOADING.equals(localImageFile))
                return;

            Resources resources = context.getResources();
            Bitmap mTmpBitmap;
            final RemoteImageAsyncTask task;
            if (TextUtils.isEmpty(localImageFile)) {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_DEFAULT);// 缺省图片
                task = new RemoteImageAsyncTask(context, imageView, imageUrl, size, true, listener);
            } else if (localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_ERROR);// 错误图片
                task = new RemoteImageAsyncTask(context, imageView, imageUrl, size, true, listener);
            } else {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_EMPTY);// 占位图片
                task = new RemoteImageAsyncTask(context, imageView, imageUrl, size, false, listener);
            }
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mTmpBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // 启动异步线程
            task.execute(null);
        }
    }

    /**
     * 取消该组件上之前的异步加载任务(用于ImageView)
     * @param imageUrl
     * @param imageView
     * @return
     */
    protected static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final RemoteImageAsyncTask asyncImageViewTask = getAsyncImageTask(imageView);
        if (asyncImageViewTask != null) {
            final String url = asyncImageViewTask.getImageUrl();
            if (url == null || !url.equals(imageUrl)) {
                // Cancel previous task
                asyncImageViewTask.cancel(true);
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
    protected static RemoteImageAsyncTask getAsyncImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                AsyncTask task = asyncDrawable.getBitmapWorkerTask();
                if (task instanceof RemoteImageAsyncTask)
                    return (RemoteImageAsyncTask) task;
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
     * 下载图片并加载进内存的异步线程。
     */
    protected class RemoteImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        protected Context mContext;
        protected final WeakReference<ImageView> mImageViewReference;
        protected String mImageUrl;
        protected XImageProcessor.ImageSize mSize;// 加载的图片尺寸
        protected boolean mDownload;
        protected XSerialDownloadListener mListener;// 下载监听

        public String getImageUrl() {
            return mImageUrl;
        }

        public RemoteImageAsyncTask(Context context, ImageView imageView, String imageUrl,
                                    XImageProcessor.ImageSize size, boolean download,
                                    XSerialDownloadListener listener) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mContext = context;
            mImageViewReference = new WeakReference<ImageView>(imageView);
            mImageUrl = imageUrl;
            mSize = size;
            mDownload = download;
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            XLog.d(TAG, "RemoteImageAsyncTask onPreExecute(), url:" + mImageUrl);
            if (mDownload) {
                if (mListener != null)
                    mListener.beforeDownload(mImageUrl); // 通知监听者
                mImageDownloadMgr.setDownloadListener(mListener);// 设置监听

                // local_image设置为“加载中”
                setLocalImage(mImageUrl, XImageLocalUrl.IMG_LOADING);
                if (mImageViewReference != null) {
                    ImageView imageView = mImageViewReference.get();
                    if (imageView != null) {
                        Resources resources = mContext.getResources();
                        Bitmap loadingBitmap = getImageResource(mContext, XImageLocalUrl.IMG_LOADING);
                        final AsyncDrawable asyncDrawable = new AsyncDrawable(resources,
                                loadingBitmap, this);
                        imageView.setImageDrawable(asyncDrawable);
                    }
                }
            }
        }

        // download and decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            XLog.d(TAG, "RemoteImageAsyncTask doInBackground(), url:" + mImageUrl);
            if (mDownload) {
                String localUrl = mImageDownloadMgr.downloadImg2File(mImageUrl, null);
                if (TextUtils.isEmpty(localUrl))
                    setLocalImage(mImageUrl, XImageLocalUrl.IMG_ERROR);// local_image设置为“错误”
                else
                    setLocalImage(mImageUrl, localUrl);// local_image设置为“加载中”
            }
            return loadRealImage(mContext, mImageUrl, mSize);
        }


        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            XLog.d(TAG, "RemoteImageAsyncTask onPostExecute(), url:" + mImageUrl);
            if (isCancelled())
                bitmap = null;


            if (mDownload) {
                // cancel时，如果local_image设置为“加载中”,则设置为空
                if (bitmap == null || bitmap.isRecycled())
                    resetImageNull();

                mImageDownloadMgr.setDownloadListener(null);// 取消监听

                if (mListener != null)
                    mListener.afterDownload(mImageUrl);// 通知监听者
            }

            // 设置真正的图片
            if (mImageViewReference != null && bitmap != null && !bitmap.isRecycled()) {
                final ImageView imageView = mImageViewReference.get();
                final RemoteImageAsyncTask asyncImageViewTask = getAsyncImageTask(imageView);
                // 加载前的检测，保证asyncTask没被替代
                if (this == asyncImageViewTask && imageView != null) {
                    imageView.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
                    showViewAnimation(mContext, imageView);
                }
            }
        }

        @Override
        protected void onCancelled() {
            XLog.d(TAG, "RemoteImageAsyncTask onCancelled. url:" + mImageUrl);
            super.onCancelled();

            if (mDownload) {
                // cancel时，如果local_image设置为“加载中”,则设置为空
                resetImageNull();

                mImageDownloadMgr.setDownloadListener(null);// 取消监听

                if (mListener != null)
                    mListener.afterDownload(mImageUrl);// 通知监听者
            }
        }

        /**
         * cancel时，如果local_image设置为“加载中”,则设置为空
         */
        private void resetImageNull() {
            String localImageFile = getLocalImage(mImageUrl);
            if (XImageLocalUrl.IMG_LOADING.equals(localImageFile))
                setLocalImage(mImageUrl, null);// local_image设置为空，待下次重新下载
        }
    }
}
