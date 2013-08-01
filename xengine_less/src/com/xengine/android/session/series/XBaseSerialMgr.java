package com.xengine.android.session.series;

import android.os.AsyncTask;

import java.util.Iterator;
import java.util.LinkedList;
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
public abstract class XBaseSerialMgr<V, T> implements XSerial<V, T> {

    private AsyncTask nextTask;
    private LinkedList<AsyncTask> tobeExecuted;
    private boolean isWorking;

    public XBaseSerialMgr() {
        tobeExecuted = new LinkedList<AsyncTask>();
        isWorking = false;
    }

    @Override
    public void startTask(V data, T listener) {
        addNewTask(createTask(data, listener));
        fireDownload();
    }

    @Override
    public void startTasks(List<V> dataList, List<T> listenerList) {
        for(int i = 0; i< dataList.size(); i++)
            addNewTask(createTask(dataList.get(i), listenerList.get(i)));
        fireDownload();
    }

    /**
     * 判断是否包含某任务
     * @param id
     * @return
     */
    private boolean containsTask(String id) {
        Iterator<AsyncTask> it = tobeExecuted.iterator();
        while (it.hasNext()) {
            if (getTaskId(it.next()).equals(id))
                return true;
        }
        return false;
    }

    /**
     * 添加新的下载任务
     * @param task
     */
    private synchronized boolean addNewTask(AsyncTask task) {
        if (!containsTask(getTaskId(task))) {// 判断是否重复
            tobeExecuted.offer(task);
            return true;
        }
        return false;
    }

    /**
     * 启动下载进程
     */
    private void fireDownload() {
        nextTask = tobeExecuted.peek();
        if (!isWorking && nextTask != null) {
            nextTask.execute(null);
            isWorking = true;
        }
    }

    @Override
    public void stop() {
        isWorking = false;
        if (nextTask!=null)
            nextTask.cancel(true);
        nextTask = null;
    }

    @Override
    public void stopAndReset() {
        stop();
        tobeExecuted.clear();
    }

    /**
     * 回调此函数。图片下载任务完成后，通知下载管理器执行下一个或停止。
     * @param task
     */
    protected synchronized void notifyTaskFinished(AsyncTask task) {
        if (!isWorking)
            return;

        tobeExecuted.remove(task);
        nextTask = tobeExecuted.peek();
        if (nextTask != null)
            nextTask.execute(null);
        else
            isWorking = false;
    }


    protected abstract AsyncTask createTask(V data, T listener);

    protected abstract String getTaskId(AsyncTask task);
}
