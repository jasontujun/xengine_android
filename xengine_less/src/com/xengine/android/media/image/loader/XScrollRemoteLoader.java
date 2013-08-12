package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.download.XSerialDownloadListener;
import com.xengine.android.system.series.XBaseSerialMgr;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 滑动延迟加载的远程图片加载器。（用于ListView或GridView）
 * 特点：
 * 1. 包括本地加载，如果本地没有则下载。
 * 2. 加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * 3. 异步方式加载。
 * 4. 延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 5. 双队列执行（两个线程）：下载队列负责异步下载任务，加载队列负责异步加载图片。
 * 6. 支持对多个不同imageView同时加载相同url的图片
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
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果该图片已经下载下来，则直接进入本地加载队列即可
        String localImageFile = getLocalImage(imageUrl);
        if (!XStringUtil.isNullOrEmpty(localImageFile) &&
                !XImageLocalUrl.IMG_LOADING.equals(localImageFile) &&
                !XImageLocalUrl.IMG_ERROR.equals(localImageFile)){
            if (cancelPotentialWork(imageUrl, imageView))
                mScrollLocalLoader.asyncLoadBitmap(context, imageUrl, imageView, size);
            return;
        }

        // 启动异步线程进行下载，将imageView设置为对应状态的图标
        // （先取消之前可能对同一个ImageView但不同图片的下载工作）
        if (XScrollLocalLoader.cancelPotentialWork(imageUrl, imageView) &&
                cancelPotentialWork(imageUrl, imageView)) {
            // 如果local_image标记为“加载中”，即图片正在下载，什么都不做
            if (XImageLocalUrl.IMG_LOADING.equals(localImageFile)) {
                imageView.setImageResource(mLoadingImageResource);
                return;
            }

            Resources resources = context.getResources();
            Bitmap mTmpBitmap = null;
            ScrollRemoteAsyncTask task = null;
            if (XStringUtil.isNullOrEmpty(localImageFile)) {
                mTmpBitmap = BitmapFactory.decodeResource(resources, mDefaultImageResource);// 缺省图片
            } else if (XImageLocalUrl.IMG_ERROR.equals(localImageFile)) {
                mTmpBitmap = BitmapFactory.decodeResource(resources, mErrorImageResource);// 错误图片
            }
            if (mTmpBitmap != null) {
                task = new ScrollRemoteAsyncTask(context, imageView, imageUrl, size, listener);
                AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mTmpBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                // 添加进队列中，等待执行
                if (!mSerialDownloadMgr.addNewTask(task)) {
                    // 如果添加失败，说明有同样url的下载任务，则把此ImageView附加到该任务中
                    ScrollRemoteAsyncTask sameUrlTask = mSerialDownloadMgr.getTaskById(imageUrl);
                    if (sameUrlTask.getStatus() == AsyncTask.Status.PENDING) {
                        sameUrlTask.addSameUrlView(imageView);
                        asyncDrawable = new AsyncDrawable(resources, mTmpBitmap, sameUrlTask);
                        imageView.setImageDrawable(asyncDrawable);
                    }
                }
            }
        }
    }

    /**
     * 取消该组件上之前的异步加载任务(用于ImageView)
     * @param imageUrl
     * @param imageView
     * @return
     */
    protected static boolean cancelPotentialWork(String imageUrl, ImageView imageView) {
        final ScrollRemoteAsyncTask asyncImageViewTask = getAsyncImageTask(imageView);
        if (asyncImageViewTask != null) {
            final String url = asyncImageViewTask.getImageUrl();
            if (url == null || !url.equals(imageUrl)) {
                // 只有主要的imageView才能cancel此asyncTask
                if (asyncImageViewTask.getImageView() == imageView)
                    // Cancel previous task
                    asyncImageViewTask.cancel(true);
                // 如果是次要的imageView，则将此imageView从asyncTask中删除
                else
                    asyncImageViewTask.deleteSameUrlView(imageView);
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
    protected static ScrollRemoteAsyncTask getAsyncImageTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                AsyncTask task = asyncDrawable.getBitmapWorkerTask();
                if (task instanceof ScrollRemoteAsyncTask)
                    return (ScrollRemoteAsyncTask) task;
            }
        }
        return null;
    }

    @Override
    public void onScroll() {
        mSerialDownloadMgr.stop();
        mScrollLocalLoader.onScroll();
    }

    @Override
    public void onIdle() {
        mSerialDownloadMgr.start();
        mScrollLocalLoader.onIdle();
    }

    @Override
    public void stopAndClear() {
        mSerialDownloadMgr.stopAndReset();
        mScrollLocalLoader.stopAndClear();
    }

    /**
     * 基于XBaseSerialMgr实现的线性图片下载执行器
     */
    private class SerialDownloadMgr extends XBaseSerialMgr {
        @Override
        protected String getTaskId(AsyncTask task) {
            return ((ScrollRemoteAsyncTask) task).getImageUrl();
        }

        @Override
        public void notifyTaskFinished(AsyncTask task) {
            super.notifyTaskFinished(task);
        }

        public ScrollRemoteAsyncTask getTaskById(String id) {
            Iterator<AsyncTask> it = mTobeExecuted.iterator();
            while (it.hasNext()) {
                AsyncTask task = it.next();
                String taskId = getTaskId(task);
                if (taskId != null && taskId.equals(id))
                    return (ScrollRemoteAsyncTask)task;
            }
            return null;
        }

        /**
         * 覆盖stop方法，停止时并不cancel当前任务，只是设置一个标记。
         */
        @Override
        public void stop() {
            mIsWorking = false;
        }
    }

    /**
     * 线性地下载图片的AsyncTask
     */
    private class ScrollRemoteAsyncTask extends RemoteImageAsyncTask {

        private LinkedList<WeakReference<ImageView>> mSameUrlViews;

        public ScrollRemoteAsyncTask(Context context, ImageView imageView, String imageUrl,
                                     XImageProcessor.ImageSize size,
                                     XSerialDownloadListener listener) {
            super(context, imageView, imageUrl, size, true, listener);
        }

        public ImageView getImageView() {
            return mImageViewReference.get();
        }

        public void addSameUrlView(ImageView imageView) {
            if (imageView == mImageViewReference.get())
                return;

            if (mSameUrlViews == null)
                mSameUrlViews = new LinkedList<WeakReference<ImageView>>();

            ListIterator<WeakReference<ImageView>> it = mSameUrlViews.listIterator();
            while (it.hasNext()){
                if (imageView == it.next().get())
                    return;
            }
            mSameUrlViews.offer(new WeakReference<ImageView>(imageView));
        }

        public void deleteSameUrlView(ImageView imageView) {
            if (mSameUrlViews == null)
                return;

            mSameUrlViews.remove(imageView);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            // 设置附带的imageViews的图片
            if (mSameUrlViews != null && bitmap != null) {
                ListIterator<WeakReference<ImageView>> it = mSameUrlViews.listIterator();
                while (it.hasNext()){
                    final ImageView imageView = it.next().get();
                    final RemoteImageAsyncTask asyncImageViewTask = getAsyncImageTask(imageView);
                    // 加载前的检测，保证asyncTask没被替代
                    if (this == asyncImageViewTask && imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        showViewAnimation(mContext, imageView);
                    }
                }
            }

            XLog.d(TAG, "notifyTaskFinished.");
            mSerialDownloadMgr.notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            XLog.d(TAG, "notifyTaskFinished.");
            mSerialDownloadMgr.notifyTaskFinished(this);
        }
    }
}
