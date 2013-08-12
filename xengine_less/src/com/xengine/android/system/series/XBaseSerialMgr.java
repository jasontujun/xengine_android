package com.xengine.android.system.series;

import android.os.AsyncTask;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 线性执行类的基础实现类。
 * 封装了线性下载任务，以及相关操作。
 * @see XWrapperSerialMgr 扩展了几个包装接口的线性执行类
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class XBaseSerialMgr implements XSerial {

    protected AsyncTask mNextTask;
    protected LinkedList<AsyncTask> mTobeExecuted;
    protected boolean mIsWorking;

    public XBaseSerialMgr() {
        mTobeExecuted = new LinkedList<AsyncTask>();
        mIsWorking = false;
    }

    /**
     * 判断是否包含某任务
     * @param id
     * @return
     */
    private boolean containsTask(String id) {
        // id为null，则认为不包含此任务
        if (id == null)
            return false;

        Iterator<AsyncTask> it = mTobeExecuted.iterator();
        while (it.hasNext()) {
            String taskId = getTaskId(it.next());
            if (taskId != null && taskId.equals(id))
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
        mIsWorking = true;

        mNextTask = mTobeExecuted.peek();
        if (mNextTask != null &&
                mNextTask.getStatus() == AsyncTask.Status.PENDING)
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

    /**
     * 获取task的id。如果返回的id为null，则认为此task唯一
     * @param task
     * @return 返回该task的id。
     */
    protected abstract String getTaskId(AsyncTask task);
}
