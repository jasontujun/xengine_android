package com.xengine.android.session.series;

import android.os.AsyncTask;

/**
 * 线性任务执行的接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 */
public interface XSerial {

    /**
     * 添加任务
     * @param task
     * @return
     */
    boolean addNewTask(AsyncTask task);

    /**
     * 暂停任务队列的执行
     */
    void start();

    /**
     * 暂停任务队列的执行
     */
    void stop();

    /**
     * 停止并清空任务队列
     */
    void stopAndReset();
}
