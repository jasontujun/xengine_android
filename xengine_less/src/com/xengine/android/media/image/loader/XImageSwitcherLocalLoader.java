package com.xengine.android.media.image.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageSwitcher;
import com.xengine.android.media.image.processor.XImageProcessor;

import java.lang.ref.WeakReference;

/**
 * 本地图片加载器(用于ImageSwitcher)。
 * 特点：
 * 1. 只负责本地加载，不涉及下载.
 * 2. 二级缓存（内存 + sd卡的图片缓存）。
 * 3. 异步方式加载。
 * 4. 同步方式加载。
 * 5. 多线程并发，无序且比较耗费资源
 * @see XImageViewLocalLoader 用于ImageView的本地图片加载器
 * Created by jasontujun.
 * Date: 12-10-9
 * Time: 下午1:22
 */
public abstract class XImageSwitcherLocalLoader extends XBaseImageLoader
        implements XImageLocalLoader<ImageSwitcher> {

    @Override
    public void asyncLoadBitmap(Context context, String imageUrl,
                                ImageSwitcher imageSwitcher, XImageProcessor.ImageSize size) {
        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null && !bitmap.isRecycled()) {
            imageSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
            imageSwitcher.setTag(null);
            return;
        }

        // 如果没有，则启动异步加载（先取消之前可能对同一个ImageView但不同图片的加载工作）
        if (cancelPotentialWork(imageUrl, imageSwitcher)) {
            // 如果是默认不存在的图标提示（“缺省图片”，“加载中”，“加载失败”等），则不需要异步
            if (loadErrorImage(imageUrl, imageSwitcher)) {// TIP 不要渐变效果
                imageSwitcher.setTag(null);
                return;
            }

            // 如果是真正图片，则需要异步加载
            final LocalImageSwitcherAsyncTask task = new LocalImageSwitcherAsyncTask(context, imageSwitcher, imageUrl, size);
            imageSwitcher.setTag(task);
            task.execute(null);
        }
    }

    @Override
    public void syncLoadBitmap(Context context, String imageUrl,
                               ImageSwitcher imageSwitcher, XImageProcessor.ImageSize size) {

        // 检测是否在缓存中已经存在此图片
        Bitmap bitmap = mImageCache.getCacheBitmap(imageUrl, size);
        if (bitmap != null && !bitmap.isRecycled()) {
            imageSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
            return;
        }

        // 检测本地图片是否对应为图标提示（“缺省图片”，“加载中”，“加载失败”等）
        if (loadErrorImage(imageUrl, imageSwitcher)) // TIP 不要渐变效果
            return;

        // 如果是真正图片，则需要异步加载
        bitmap = loadRealImage(context, imageUrl, size);
        imageSwitcher.setImageDrawable(new BitmapDrawable(bitmap));
    }

    /**
     * 判断imgUrl对应的本地图片是否存在。
     * 若不存在，返回true。若真正存在，返回false
     * @param imageUrl
     * @return
     */
    private boolean loadErrorImage(String imageUrl, ImageSwitcher imageSwitcher) {
        String localImageFile = getLocalImage(imageUrl);
        if (TextUtils.isEmpty(localImageFile)) {
            // 返回缺省图片（图片不存在）
            imageSwitcher.setImageResource(mDefaultImageResource);
            return true;
        }
        if (localImageFile.equals(XImageLocalUrl.IMG_ERROR)) {
            // 返回错误图片（图片错误）
            imageSwitcher.setImageResource(mErrorImageResource);
            return true;
        }
        if (localImageFile.equals(XImageLocalUrl.IMG_LOADING)) {
            // 返回加载提示的图片（图片加载中）
            imageSwitcher.setImageResource(mLoadingImageResource);
            return true;
        }
        return false;
    }

    /**
     * 取消该组件上之前的异步加载任务
     * @param imageUrl
     * @param imageSwitcher
     * @return
     */
    private static boolean cancelPotentialWork(String imageUrl,
                                                 ImageSwitcher imageSwitcher) {
        final LocalImageSwitcherAsyncTask asyncImageTask = getAsyncImageTask(imageSwitcher);
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
    private static LocalImageSwitcherAsyncTask getAsyncImageTask(ImageSwitcher imageSwitcher) {
        if (imageSwitcher != null) {
            Object tag = imageSwitcher.getTag();
            if (tag != null && tag instanceof LocalImageSwitcherAsyncTask)
                return (LocalImageSwitcherAsyncTask) tag;
        }
        return null;
    }

    /**
     * 异步本地加载图片(用于ImageSwitcher)
     */
    protected class LocalImageSwitcherAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private Context context;
        private final WeakReference<ImageSwitcher> switcherReference;
        private String imageUrl;
        private XImageProcessor.ImageSize size;// 加载的图片尺寸

        public LocalImageSwitcherAsyncTask(Context context, ImageSwitcher switcher, String imageUrl,
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
                final LocalImageSwitcherAsyncTask asyncImageTask = getAsyncImageTask(switcher);
                if (this == asyncImageTask && switcher != null) {
                    switcher.setImageDrawable(new BitmapDrawable(bitmap));
                    switcher.setTag(null);
                }
            }
        }
    }
}
