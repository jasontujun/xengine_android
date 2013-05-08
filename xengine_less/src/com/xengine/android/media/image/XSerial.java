package com.xengine.android.media.image;

import java.util.List;

/**
 * 线性任务执行的接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 */
public interface XSerial<T> {

    /**
     * 添加单个任务进线性队列中，并启动队列执行
     * @param imgUrl
     * @param listener
     */
    void startTask(String imgUrl, T listener);

    /**
     * 添加一堆任务进线性队列中，并启动队列执行
     * @param imgUrlList
     * @param listenerList
     */
    void startTasks(List<String> imgUrlList, List<T> listenerList);

    /**
     * 暂停任务队列的执行
     */
    void stop();

    /**
     * 停止并清空任务队列
     */
    void stopAndReset();
}
