package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.xengine.android.media.image.download.XImageDownloadMgr;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.series.XBaseSerialMgr;
import com.xengine.android.session.series.XSerialDownloadListener;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

/**
 * 用于ListView或GridView的远程图片加载器。
 * 特点：
 * 1.带有延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 2.加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XScrollRemoteLoader extends XImageViewRemoteLoader
        implements XScrollLazyLoading {
    private static final String TAG = XScrollRemoteLoader.class.getSimpleName();

    private SerialTaskMgr mSerialMgr;// 线性执行器

    public XScrollRemoteLoader(XImageDownloadMgr imageDownloadMgr) {
        super(imageDownloadMgr);
        mSerialMgr = new SerialTaskMgr();
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
            final SerialAsyncImageTask task;
            if (XStringUtil.isNullOrEmpty(localImageFile)) {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mDefaultImageResource);// 缺省图片
                task = new SerialAsyncImageTask(context, imageView, imageUrl, size, true, listener);
            } else if (localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mErrorImageResource);// 错误图片
                task = new SerialAsyncImageTask(context, imageView, imageUrl, size, true, listener);
            } else {
                mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, mEmptyImageResource);// 占位图片
                task = new SerialAsyncImageTask(context, imageView, imageUrl, size, false, listener);
            }
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // 添加进队列中，等待执行
            mSerialMgr.addNewTask(task);
        }
    }

    @Override
    public void fallAsleep() {
        mSerialMgr.stop();
    }

    @Override
    public void wakeUp() {
        mSerialMgr.start();
    }

    @Override
    public void fallAsleepAndClear() {
        mSerialMgr.stopAndReset();
    }

    private class TaskParam {
        Context context;
        String imageUrl;
        ImageView imageView;
        XImageProcessor.ImageSize size;
        boolean isDownload;
    }

    private class SerialTaskMgr extends XBaseSerialMgr<TaskParam, XSerialDownloadListener> {

        @Override
        protected AsyncTask createTask(TaskParam data, XSerialDownloadListener listener) {
            return new SerialAsyncImageTask(data.context,
                    data.imageView, data.imageUrl, data.size, data.isDownload, listener);
        }

        @Override
        protected String getTaskId(AsyncTask task) {
            return ((SerialAsyncImageTask) task).getImageUrl();
        }

        @Override
        public void notifyTaskFinished(AsyncTask task) {
            super.notifyTaskFinished(task);
        }
    }

    /**
     * 线性下载并加载图片的AsyncTask
     */
    private class SerialAsyncImageTask extends RemoteAsyncImageTask {

        public SerialAsyncImageTask(Context context, ImageView imageView, String imageUrl,
                                    XImageProcessor.ImageSize size, boolean download,
                                    XSerialDownloadListener listener) {
            super(context, imageView, imageUrl, size, download, listener);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            XLog.d(TAG, "notifyTaskFinished.");
            mSerialMgr.notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            XLog.d(TAG, "notifyTaskFinished.");
            mSerialMgr.notifyTaskFinished(this);
        }
    }
}
