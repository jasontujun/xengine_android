package com.xengine.android.unknow.task;

/**
 * <pre>
 * 下载任务的监听类。
 * User: jasontujun
 * Date: 13-9-27
 * Time: 下午4:11
 * </pre>
 */
public interface TaskListener<T extends TaskBean> {

    /**
     * 开始下载前的回调函数（在异步线程）
     * @param task
     */
    void onStart(T task);

    /**
     * 暂停的回调函数（在异步线程）
     */
    void onStop(T task);

    /**
     * 终止的回调函数（在异步线程）
     */
    void onAbort(T task);

    /**
     * 下载过程中的回调函数（在异步线程）
     * @param task
     * @param completeSize
     */
    void onDownloading(T task, long completeSize);

    /**
     * 下载成功的回调函数（在异步线程）
     * @param task
     * @param localFilePath
     */
    void onComplete(T task, String localFilePath);

    /**
     * 下载失败的回调函数（在异步线程）
     * @param task
     * @param errorStr
     */
    void onError(T task, String errorStr);

    /**
     * 下载速度更新的回调函数（在异步线程）
     * @param task
     * @param speed
     */
    void onSpeedUpdate(T task, long speed);
}
