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
public abstract class XBaseSerialMgr<V, T> implements XSerial {

    private AsyncTask mNextTask;
    protected LinkedList<AsyncTask> mTobeExecuted;
    private boolean mIsWorking;

    public XBaseSerialMgr() {
        mTobeExecuted = new LinkedList<AsyncTask>();
        mIsWorking = false;
    }

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

    /**
     * 判断是否包含某任务
     * @param id
     * @return
     */
    private boolean containsTask(String id) {
        Iterator<AsyncTask> it = mTobeExecuted.iterator();
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
    @Override
    public synchronized boolean addNewTask(AsyncTask task) {
        if (!containsTask(getTaskId(task))) {// 判断是否重复
            mTobeExecuted.offer(task);
            return true;
        }
        return false;
    }

    /**
     * 启动下载进程
     */
    @Override
    public void start() {
        if (mIsWorking)
            return;
        mIsWorking = true;

        mNextTask = mTobeExecuted.peek();
        if (mNextTask != null && mNextTask.getStatus() == AsyncTask.Status.PENDING)
            mNextTask.execute(null);
    }

    @Override
    public void stop() {
        if (mNextTask != null)
            mNextTask.cancel(true);
        mNextTask = null;
        mIsWorking = false;
    }

    @Override
    public void stopAndReset() {
        stop();
        mTobeExecuted.clear();
    }

    /**
     * 回调函数。图片下载任务完成后，通知下载管理器执行下一个或停止。
     * task在结束时回调此函数(onPostExecuted()或onCancel()里)
     * @param task
     */
    protected synchronized void notifyTaskFinished(AsyncTask task) {
        // 只要结束就从队列里删除（无论是cancel的还是正常结束的）
        mTobeExecuted.remove(task);

        // 如果已经标记停止，则什么都不做
        if (!mIsWorking)
            return;

        // 如果task不是头部执行的mNextTask,则什么都不做
        if (task != mNextTask)
            return;

        mNextTask = mTobeExecuted.peek();
        if (mNextTask != null && mNextTask.getStatus() == AsyncTask.Status.PENDING)
            mNextTask.execute(null);// 执行下一个任务
        else
            mIsWorking = false;// 没有任务，标记结束
    }


    protected abstract AsyncTask createTask(V data, T listener);

    protected abstract String getTaskId(AsyncTask task);
}
