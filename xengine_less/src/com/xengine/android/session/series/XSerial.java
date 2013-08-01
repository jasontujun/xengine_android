package com.xengine.android.session.series;

import java.util.List;

/**
 * 线性任务执行的接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 */
public interface XSerial<V, T> {

    /**
     * 添加单个任务进线性队列中，并启动队列执行
     * @param data 数据
     * @param listener 监听器
     */
    void startTask(V data, T listener);

    /**
     * 添加一堆任务进线性队列中，并启动队列执行
     * @param dataList 数据列表
     * @param listenerList 监听器列表
     */
    void startTasks(List<V> dataList, List<T> listenerList);

    /**
     * 暂停任务队列的执行
     */
    void stop();

    /**
     * 停止并清空任务队列
     */
    void stopAndReset();
}
