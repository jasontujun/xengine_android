package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.system.download.XSerialDownloadListener;
import com.xengine.android.system.series.XBaseSerialMgr;
import com.xengine.android.utils.XLog;

/**
 * 滑动延迟加载的远程图片加载器。（适用于ListView或GridView）
 * 特点：
 * 1. 包括本地加载，如果本地没有则下载。
 * 2. 加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * 3. 异步方式加载。
 * 4. 延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 5. 双队列执行（两个线程）：下载队列负责异步下载任务，加载队列负责异步加载图片。
 * 6. 支持对多个不同imageView同时加载相同url的图片（图片:imageView = 1:N）
 * @see XScrollLocalLoader
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XScrollRemoteLoader extends XImageViewRemoteLoader
        implements XScrollLazyLoading {
    private static final String TAG = XScrollRemoteLoader.class.getSimpleName();

    private SerialDownloadMgr mSerialDownloadMgr;// 图片下载的线性队列
    private XScrollLocalLoader mScrollLocalLoader;// 本地加载的线性队列（直接复用XScrollLocalLoader）

    public XScrollRemoteLoader(XImageDownload imageDownloadMgr,
                               XScrollLocalLoader scrollLocalLoader) {
        super(imageDownloadMgr);
        mSerialDownloadMgr = new SerialDownloadMgr();
        mScrollLocalLoader = scrollLocalLoader;
    }

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView,
                                XImageProcessor.ImageSize size,
                                XSerialDownloadListener listener) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null && !bitmap.isRecycled()) {
            // 取消之前可能对同一个ImageView但不同图片的下载工作
            mScrollLocalLoader.cancelPotentialLazyWork(imageUrl, imageView);
            cancelPotentialLazyWork(imageUrl, imageView);
            // 设置图片
            imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
            showViewAnimation(context, imageView);
            XLog.d(TAG, "XScrollRemoteLoader has cache bitmap. url:" + imageUrl);
            return;
        }

        // 如果该图片已经下载下来，则直接进入本地加载队列即可
        String localImageFile = getLocalImage(imageUrl);
        if (!TextUtils.isEmpty(localImageFile) &&
                !XImageLocalUrl.IMG_LOADING.equals(localImageFile) &&
                !XImageLocalUrl.IMG_ERROR.equals(localImageFile)) {
            XLog.d(TAG, "XScrollRemoteLoader1 cancelPotentialLazyWork true. url:" + imageUrl);
            cancelPotentialLazyWork(imageUrl, imageView);
            mScrollLocalLoader.asyncLoadBitmap(context, imageUrl, imageView, size);
            return;
        }

        XLog.d(TAG, "asyncLoadBitmap.... url:" + imageUrl);
        // 取消之前可能对同一个ImageView但不同图片的下载工作
        if (mScrollLocalLoader.cancelPotentialLazyWork(imageUrl, imageView) &&
                cancelPotentialLazyWork(imageUrl, imageView)) {
            XLog.d(TAG, "XScrollRemoteLoader2 cancelPotentialLazyWork true. url:" + imageUrl);
            Resources resources = context.getResources();
            Bitmap mTmpBitmap = null;
            ScrollRemoteAsyncTask task = null;
            // 启动异步线程进行下载，将imageView设置为对应状态的图标
            if (TextUtils.isEmpty(localImageFile)) {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_DEFAULT);// 缺省图片
            } else if (XImageLocalUrl.IMG_LOADING.equals(localImageFile)) {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_LOADING);// Loading图片
            } else if (XImageLocalUrl.IMG_ERROR.equals(localImageFile)) {
                mTmpBitmap = getImageResource(context, XImageLocalUrl.IMG_ERROR);// 错误图片
            }
            if (mTmpBitmap != null) {
                task = new ScrollRemoteAsyncTask(context, imageView, imageUrl, size, listener);
                AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mTmpBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                // 添加进队列中，等待执行
                mSerialDownloadMgr.addNewTask(task);
                XLog.d(TAG, "asyncLoadBitmap. tryStart(). url:" + imageUrl);
                mSerialDownloadMgr.tryStart();// 尝试启动
            }
        }
    }

    /**
     * 取消该组件上之前的异步加载任务(用于ImageView)
     * @param imageUrl
     * @param imageView
     * @return
     */
    protected boolean cancelPotentialLazyWork(String imageUrl, ImageView imageView) {
        final ScrollRemoteAsyncTask scrollRemoteAsyncTask =
                (ScrollRemoteAsyncTask) getAsyncImageTask(imageView);
        if (scrollRemoteAsyncTask != null) {
            final String url = scrollRemoteAsyncTask.getImageUrl();
            // asyncTask虽然存在于imageView中但已经无效
            if (scrollRemoteAsyncTask.isInvalidate()) {
                XLog.d(TAG, "cancelPotentialLazyWork - asyncImageViewTask.isInvalidate(). url:" + url);
                return true;
            }
            // 对同一个ImageView但不同url的加载任务
            else if (url == null || !url.equals(imageUrl)) {
                XLog.d(TAG, "cancelPotentialLazyWork() - cancel asynctask. old url:"
                        + url + ",new url:" + imageUrl);
                mSerialDownloadMgr.removeTask(scrollRemoteAsyncTask);// 从队列中移除
                scrollRemoteAsyncTask.setInvalidate();// 设置无效标记位
                scrollRemoteAsyncTask.cancel(true); // Cancel previous task
            }
            // The same work is already in progress
            else {
                XLog.d(TAG, "cancelPotentialLazyWork - cancel asynctask. The same work is already in progress");
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    @Override
    public void setWorking() {
        mSerialDownloadMgr.setWorking();
        mScrollLocalLoader.setWorking();
    }

    @Override
    public void onScroll() {
        XLog.d(TAG, "onScroll().");
        mSerialDownloadMgr.stop();
        mScrollLocalLoader.onScroll();
    }

    @Override
    public void onIdle() {
        XLog.d(TAG, "onIdle().");
        mSerialDownloadMgr.start();
        mScrollLocalLoader.onIdle();
    }

    @Override
    public void stopAndClear() {
        XLog.d(TAG, "stopAndClear().");
        mSerialDownloadMgr.stopAndReset();
        mScrollLocalLoader.stopAndClear();
    }

    /**
     * 基于XBaseSerialMgr实现的线性图片下载执行器
     */
    private class SerialDownloadMgr extends XBaseSerialMgr {
        @Override
        public String getTaskId(AsyncTask task) {
            return null;
        }

        @Override
        public void notifyTaskFinished(AsyncTask task) {
            super.notifyTaskFinished(task);
        }

        /**
         * 启动，将标记设置为启动
         */
        public void setWorking() {
            XLog.d(TAG, "setWorking().");
            mIsWorking = true;
        }

        /**
         * 根据当前标记，尝试启动。但不改变当前标记。
         */
        public void tryStart() {
            XLog.d(TAG, "tryStart(). 0");
            if (!mIsWorking)
                return;

            XLog.d(TAG, "tryStart(). 1");
            mNextTask = mTobeExecuted.peek();
            if (mNextTask != null &&
                    mNextTask.getStatus() == AsyncTask.Status.PENDING) {
                XLog.d(TAG, "tryStart(). 2");
                mNextTask.execute(null);
            }
        }

        /**
         * 覆盖stop方法，停止时并不cancel当前任务，只是设置一个标记。
         */
        @Override
        public void stop() {
            XLog.d(TAG, "stop().");
            mIsWorking = false;
        }
    }

    /**
     * 线性地下载图片的AsyncTask
     */
    private class ScrollRemoteAsyncTask extends RemoteImageAsyncTask {

        private boolean isInvalidate;

        public ScrollRemoteAsyncTask(Context context, ImageView imageView, String imageUrl,
                                     XImageProcessor.ImageSize size,
                                     XSerialDownloadListener listener) {
            super(context, imageView, imageUrl, size, true, listener);
            isInvalidate = false;
        }

        public boolean isInvalidate() {
            return isInvalidate;
        }

        public void setInvalidate() {
            isInvalidate = true;
        }

        @Override
        protected void onPreExecute() {
            // 如果本地已经有此图片的文件，则不执行下载
            String localImageFile = getLocalImage(mImageUrl);
            if (!TextUtils.isEmpty(localImageFile) &&
                    !XImageLocalUrl.IMG_LOADING.equals(localImageFile) &&
                    !XImageLocalUrl.IMG_ERROR.equals(localImageFile)) {
                mDownload = false;
            }

            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isInvalidate)
                return null;

            // 检测是否在缓存中已经存在此图片(有可能存在对同一url的多个task)
            Bitmap bitmap = mImageCache.getCacheBitmap(mImageUrl, mSize);
            if (bitmap != null && !bitmap.isRecycled())
                return bitmap;

            return super.doInBackground(params);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isInvalidate)
                bitmap = null;

            super.onPostExecute(bitmap);
            XLog.d(TAG, "onPostExecute() notifyTaskFinished.");
            isInvalidate = true;
            mSerialDownloadMgr.notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            XLog.d(TAG, "onCancelled() notifyTaskFinished.");
            isInvalidate = true;
            mSerialDownloadMgr.notifyTaskFinished(this);
        }
    }
}
