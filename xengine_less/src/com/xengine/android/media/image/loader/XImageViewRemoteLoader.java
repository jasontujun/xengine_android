package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownloadMgr;
import com.xengine.android.media.image.loader.cache.XAndroidImageCache;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.series.XSerialDownloadListener;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XImageViewRemoteLoader extends XBaseImageLoader
        implements XImageRemoteLoader<ImageView> {
    private static final String TAG = XImageViewRemoteLoader.class.getSimpleName();

    protected XImageDownloadMgr mImageDownloadMgr;// 图片下载管理器
    protected boolean mIsFading;// 标识是否开启渐变效果

    public XImageViewRemoteLoader(XImageDownloadMgr imageDownloadMgr) {
        mImageCache = XAndroidImageCache.getInstance();
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
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一张图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 图标提示（“加载中”）
            String localImageFile = getLocalImage(imageUrl);
            if (XImageLocalUrl.IMG_LOADING.equals(localImageFile)) {
                imageView.setImageResource(mLoadingImageResource);
                return;
            }

            // 如果是真正图片，则需要异步加载
            Resources resources = context.getResources();
            Bitmap mPlaceHolderBitmap;
            final RemoteAsyncImageTask task;
            if (XStringUtil.isNullOrEmpty(localImageFile)) {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mDefaultImageResource);// 缺省图片
                task = new RemoteAsyncImageTask(context, imageView, imageUrl, size, true, listener);
            } else if (localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mErrorImageResource);// 错误图片
                task = new RemoteAsyncImageTask(context, imageView, imageUrl, size, true, listener);
            } else {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mEmptyImageResource);// 占位图片
                task = new RemoteAsyncImageTask(context, imageView, imageUrl, size, false, listener);
            }
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(null);
        }
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
        final RemoteAsyncImageTask asyncImageViewTask = getAsyncImageTask(imageView);
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
    protected static RemoteAsyncImageTask getAsyncImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return (RemoteAsyncImageTask)asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    protected void showViewAnimation(Context context, ImageView imageView) {
        if(mIsFading) {
            AlphaAnimation fadeAnimation = new AlphaAnimation(0 ,1);
            fadeAnimation.setDuration(context.getResources().
                    getInteger(android.R.integer.config_shortAnimTime));
            imageView.startAnimation(fadeAnimation);
        }
    }

    /**
     * 下载图片并加载进内存的异步线程。
     */
    protected class RemoteAsyncImageTask extends AsyncTask<Void, Void, Bitmap> {
        private Context mContext;
        private final WeakReference<ImageView> mImageViewReference;
        private String mImageUrl;
        private XImageProcessor.ImageSize mSize;// 加载的图片尺寸
        private boolean mDownload;

        private XSerialDownloadListener mListener;// 下载监听

        public String getImageUrl() {
            return mImageUrl;
        }

        public RemoteAsyncImageTask(Context context, ImageView imageView, String imageUrl,
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
            if (mDownload) {
                if (mListener != null)
                    mListener.beforeDownload(mImageUrl); // 通知监听者
                mImageDownloadMgr.setDownloadListener(mListener);// 设置监听

                setLocalImage(getImageUrl(), XImageLocalUrl.IMG_LOADING);
                final ImageView imageView = mImageViewReference.get();
                if (imageView != null)
                    imageView.setImageResource(mLoadingImageResource);
            }
        }

        // download and decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            if (mDownload) {
                String localUrl = mImageDownloadMgr.downloadImg2File(getImageUrl(), null);
                if (XStringUtil.isNullOrEmpty(localUrl))
                    setLocalImage(getImageUrl(), XImageLocalUrl.IMG_ERROR);
                else
                    setLocalImage(getImageUrl(), localUrl);
            }
            return loadRealImage(mContext, mImageUrl, mSize);
        }


        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            XLog.d(TAG, "SerialTask onPostExecute. url:" + getImageUrl());
            if (isCancelled()) {
                bitmap = null;
            }

            mImageDownloadMgr.setDownloadListener(null);// 取消监听
            if (mDownload && mListener != null) // 通知监听者
                mListener.afterDownload(mImageUrl);

            if (mImageViewReference != null && bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                final RemoteAsyncImageTask asyncImageViewTask = getAsyncImageTask(imageView);
                if (this == asyncImageViewTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    showViewAnimation(mContext, imageView);
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mImageDownloadMgr.setDownloadListener(null);// 取消监听
            XLog.d(TAG, "SerialTask onCancelled. url:" + getImageUrl());
        }
    }
}
