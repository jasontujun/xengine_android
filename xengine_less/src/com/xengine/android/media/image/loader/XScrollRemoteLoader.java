package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.series.XBaseSerialMgr;
import com.xengine.android.session.series.XSerialDownloadListener;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

/**
 * 滑动延迟加载的远程图片加载器。（用于ListView或GridView）
 * 特点：
 * 1. 包括本地加载，如果本地没有则下载。
 * 2. 加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * 3. 异步方式加载。
 * 4. 延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 5. 双队列（两个个异步线程）：下载队列负责异步下载任务，加载队列负责异步加载图片。
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
            mScrollLocalLoader.asyncLoadBitmap(context, imageUrl, imageView, size);
            return;
        }

        // 启动异步线程进行下载，将imageView设置为对应状态的图标
        // （先取消之前可能对同一个ImageView但不同图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 如果local_image标记为“加载中”，即图片正在下载，什么都不做
            if (XImageLocalUrl.IMG_LOADING.equals(localImageFile)) {
                imageView.setImageResource(mLoadingImageResource);
                return;
            }

            Resources resources = context.getResources();
            Bitmap mTmpBitmap = null;
            RemoteImageAsyncTask task = null;
            if (XStringUtil.isNullOrEmpty(localImageFile)) {
                mTmpBitmap = BitmapFactory.decodeResource(resources, mDefaultImageResource);// 缺省图片
            } else if (XImageLocalUrl.IMG_ERROR.equals(localImageFile)) {
                mTmpBitmap = BitmapFactory.decodeResource(resources, mErrorImageResource);// 错误图片
            }
            if (mTmpBitmap != null) {
                task = new ScrollRemoteAsyncTask(context, imageView, imageUrl, size, listener);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mTmpBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                // 添加进队列中，等待执行
                mSerialDownloadMgr.addNewTask(task);
            }
        }
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
        private String localUrl;

        public ScrollRemoteAsyncTask(Context context, ImageView imageView, String imageUrl,
                                     XImageProcessor.ImageSize size,
                                     XSerialDownloadListener listener) {
            super(context, imageView, imageUrl, size, true, listener);
            localUrl = null;
        }

        // download and decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            XLog.d(TAG, "RemoteImageAsyncTask doInBackground(), url:" + mImageUrl);
            localUrl = mImageDownloadMgr.downloadImg2File(mImageUrl, null);
            if (XStringUtil.isNullOrEmpty(localUrl))
                setLocalImage(mImageUrl, XImageLocalUrl.IMG_ERROR);// local_image设置为“错误”
            else
                setLocalImage(mImageUrl, localUrl);// local_image设置为“加载中”
            return null;
        }


        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            XLog.d(TAG, "RemoteImageAsyncTask onPostExecute(), url:" + mImageUrl);

            mImageDownloadMgr.setDownloadListener(null);// 取消监听
            if (mListener != null) // 通知监听者
                mListener.afterDownload(mImageUrl);

            // 创建一个加载任务，并加进图片加载队列中
            if (mImageViewReference != null) {
                final ImageView imageView = mImageViewReference.get();
                final RemoteImageAsyncTask asyncImageViewTask = getAsyncImageTask(imageView);
                if (this == asyncImageViewTask && imageView != null) {
                    XLog.d(TAG, "add local load task. url:" + mImageUrl);
                    // KEY! 启动另一个任务，交给加载线程负责图片加载
                    mScrollLocalLoader.asyncLoadBitmap(mContext, mImageUrl, imageView, mSize);
                    mScrollLocalLoader.onIdle();// 启动
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
