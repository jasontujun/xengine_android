package com.xengine.android.media.image.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.series.XBaseSerialMgr;
import com.xengine.android.utils.XLog;

/**
 * 用于ListView或GridView的本地图片加载器。
 * 特点：
 * 1.带有延迟加载特性：只有停止时才加载，滑动时候不加载。
 * 2.加载器先从一级缓存（内存）和二级缓存（sd卡）中寻找，如果没有则从网上下载。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-6
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class XScrollLocalLoader extends XImageViewLocalLoader
        implements XScrollLazyLoading {
    private static final String TAG = XScrollLocalLoader.class.getSimpleName();

    private SerialTaskMgr mSerialMgr;// 线性执行器

    public XScrollLocalLoader() {
        super();
        mSerialMgr = new SerialTaskMgr();
    }

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageView imageView,
                                XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            showViewAnimation(context, imageView);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一张图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageView)) {
            // 如果是默认不存在的图标提示（“缺省图片”，“加载中”，“加载失败”等），则不需要异步
            if (loadErrorImage(imageUrl, imageView)) // TIP 不要渐变效果
                return;

            // 如果是真正图片，则需要异步加载
            Resources resources = context.getResources();
            Bitmap mPlaceHolderBitmap = BitmapFactory.
                    decodeResource(resources, mEmptyImageResource);// 占位图片
            final AsyncImageViewTask task = new AsyncImageViewTask(context, imageView, imageUrl, size);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // 添加进队列中，等待执行
            mSerialMgr.addNewTask(task);
        }
    }

    @Override
    public void onScroll() {
        mSerialMgr.stop();
    }

    @Override
    public void onIdle() {
        mSerialMgr.start();
    }

    @Override
    public void stopAndClear() {
        mSerialMgr.stopAndReset();
    }

    private class TaskParam {
        Context context;
        String imageUrl;
        ImageView imageView;
        XImageProcessor.ImageSize size;
    }

    private class SerialTaskMgr extends XBaseSerialMgr<TaskParam, Void> {

        @Override
        protected AsyncTask createTask(TaskParam data, Void listener) {
            return new SerialAsyncImageViewTask(data.context,
                    data.imageView, data.imageUrl, data.size);
        }

        @Override
        protected String getTaskId(AsyncTask task) {
            return ((SerialAsyncImageViewTask) task).getImageUrl();
        }

        @Override
        public void notifyTaskFinished(AsyncTask task) {
            super.notifyTaskFinished(task);
        }
    }

    /**
     * 线性下载并加载图片的AsyncTask
     */
    private class SerialAsyncImageViewTask extends AsyncImageViewTask {

        public SerialAsyncImageViewTask(Context context, ImageView imageView, String imageUrl,
                                        XImageProcessor.ImageSize size) {
            super(context, imageView, imageUrl, size);
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
