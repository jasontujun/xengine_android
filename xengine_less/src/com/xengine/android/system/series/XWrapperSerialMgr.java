package com.xengine.android.system.series;

import android.os.AsyncTask;

import java.util.List;

/**
 * 线性执行类。
 * 封装了线性下载任务，以及相关操作。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class XWrapperSerialMgr<V, T> extends XBaseSerialMgr {

    /**
     * 添加单个任务进线性队列中，并启动队列执行
     * @param data 数据
     * @param listener 监听器
     */
    public void addAndStartTask(V data, T listener) {
        addNewTask(createTask(data, listener));
        start();
    }

    /**
     * 添加一堆任务进线性队列中，并启动队列执行
     * @param dataList 数据列表
     * @param listenerList 监听器列表
     */
    public void addAndStartTasks(List<V> dataList, List<T> listenerList) {
        if (dataList == null || dataList.size() == 0)
            return;
        if (listenerList != null && dataList.size() != listenerList.size())
            return;

        for (int i = 0; i< dataList.size(); i++) {
            T listener = (listenerList == null) ? null : listenerList.get(i);
            if (listenerList != null)
            addNewTask(createTask(dataList.get(i), listener));
        }
        start();
    }

    protected abstract AsyncTask createTask(V data, T listener);
}
