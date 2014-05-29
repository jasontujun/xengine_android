package com.xengine.android.system.series;

import android.os.AsyncTask;
import com.xengine.android.utils.XLog;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 线性执行类的基础实现类（针对AsyncTask）。
 * 封装了线性下载任务，以及相关操作。
 * @see XWrapperSerialMgr 扩展了几个包装接口的线性执行类
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class XBaseSerialMgr implements XSerial<AsyncTask> {
    private static final String TAG = XBaseSerialMgr.class.getSimpleName();

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

    @Override
    public synchronized boolean addNewTask(AsyncTask task) {
        if (containsTask(getTaskId(task)))// 判断是否重复
            return false;

        mTobeExecuted.offer(task);
        return true;
    }

    @Override
    public void removeTask(String taskId) {
        if (taskId == null)
            return;

        Iterator<AsyncTask> it = mTobeExecuted.iterator();
        while (it.hasNext()) {
            AsyncTask task = it.next();
            if (taskId.equals(getTaskId(task))) {
                removeTask(task);
                return;
            }
        }
    }

    @Override
    public void removeTask(AsyncTask task) {
        mTobeExecuted.remove(task);
        if (mNextTask == task) {
            mNextTask = findNextTask();
            task.cancel(true);
        }
    }

    @Override
    public void start() {
        mIsWorking = true;

        mNextTask = findNextTask();
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
        Iterator<AsyncTask> it = mTobeExecuted.iterator();
        while (it.hasNext()) {
            it.next().cancel(true);
        }
        mTobeExecuted.clear();
    }

    @Override
    public AsyncTask findNextTask() {
        return mTobeExecuted.peek();
    }

    /**
     * 回调函数。任务完成后，执行下一个或停止。
     * task在结束时回调此函数(onPostExecuted()或onCancel()里)
     * @param task 已结束的task
     */
    protected synchronized void notifyTaskFinished(AsyncTask task) {
        // 只要结束就从队列里删除（无论是cancel的还是正常结束的）
        mTobeExecuted.remove(task);

        // 如果已经标记停止，则什么都不做
        if (!mIsWorking)
            return;

        // 如果task是当前执行的mNextTask,则寻找下一个任务
        if (task == mNextTask) {
            XLog.d(TAG, "notifyTaskFinished task == mNextTask, " + task);
            mNextTask = findNextTask();
        }

        if (mNextTask != null) {
            XLog.d(TAG, "notifyTaskFinished mNextTask!=null," + mNextTask);
            if (mNextTask.getStatus() == AsyncTask.Status.PENDING)
                mNextTask.execute(null);// 执行下一个任务
        } else {
            XLog.d(TAG, "notifyTaskFinished mNextTask==null");
            mIsWorking = false;// 没有任务，标记结束
        }
    }
}
