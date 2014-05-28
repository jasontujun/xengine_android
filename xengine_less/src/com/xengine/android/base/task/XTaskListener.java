package com.xengine.android.base.task;

/**
 * <pre>
 * 任务监听接口
 * User: jasontujun
 * Date: 13-9-27
 * Time: 下午4:11
 * </pre>
 */
public interface XTaskListener<T extends XTaskBean> {

    /**
     * 开始前的回调函数
     * @param task
     */
    void onStart(T task);

    /**
     * 暂停的回调函数
     * @param task
     */
    void onStop(T task);

    /**
     * 终止的回调函数
     * @param task
     */
    void onAbort(T task);

    /**
     * 执行过程中的回调函数
     * @param task
     * @param completeSize
     */
    void onDoing(T task, long completeSize);

    /**
     * 执行成功结束的回调函数
     * @param task
     */
    void onComplete(T task);

    /**
     * 执行失败结束的回调函数
     * @param task
     * @param errorCode
     */
    void onError(T task, String errorCode);

    /**
     * 执行速度更新的回调函数（在异步线程）
     * @param task
     * @param speed
     */
    void onSpeedUpdate(T task, long speed);
}
