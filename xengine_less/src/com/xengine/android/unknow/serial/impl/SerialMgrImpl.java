package com.xengine.android.unknow.serial.impl;

import android.text.TextUtils;
import tv.pps.module.download.core.serial.SerialMgr;
import tv.pps.module.download.core.serial.SerialMgrListener;
import tv.pps.module.download.core.serial.SerialTask;
import tv.pps.module.download.core.serial.TaskScheduler;
import tv.pps.module.download.core.speed.BaseSpeedMonitor;
import tv.pps.module.download.core.speed.calc.DefaultSpeedCalculator;
import tv.pps.module.download.core.task.TaskBean;

import java.util.*;

/**
 * <pre>
 * 实现SerialMgr接口的基础实现类。
 * 封装了线性下载、增删任务、启动、恢复、暂停、速度监视等操作。
 * 1.每个时刻，最多只有一个下载任务正在执行；
 * 2.每个任务都在TODO,DOING,DONE,ERROR四个状态间转换；
 * 3.如果任务从DOING到ERROR，回调SerialMgr时，
 *   会将该任务丢弃，并停止下载；
 * 4.如果任务从DOING到TODO，回调SerialMgr时，
 *   会将该任务重新添加进等待队列，并停止下载；
 * @see tv.pps.module.download.core.task.TaskExecutor
 * User: tujun
 * Date: 13-8-1
 * Time: 下午3:35
 * </pre>
 */
public class SerialMgrImpl implements SerialMgr<SerialTask, TaskBean> {

    private boolean mIsWorking;// 标识运行状态
    private volatile SerialTask mCurrentExecuted;// 当前正在运行的任务
    private LinkedList<SerialTask> mTobeExecuted;// 待执行的任务队列
    private TaskScheduler<TaskBean> mScheduler;// 任务排序器(外部设置)
    private Comparator<SerialTask> mInnerComparator;// 实际用来排序的比较器
    private BaseSpeedMonitor<SerialTask> mSpeedMonitor;// 下载速度监视器
    private List<SerialMgrListener<TaskBean>> mListeners;// 外部监听者

    public SerialMgrImpl() {
        mCurrentExecuted = null;
        mTobeExecuted = new LinkedList<SerialTask>();
        mInnerComparator = new InnerTaskComparator();
        mListeners = new ArrayList<SerialMgrListener<TaskBean>>();
        mIsWorking = false;
    }

    public void setSpeedMonitor(BaseSpeedMonitor<SerialTask> speedMonitor) {
        mSpeedMonitor = speedMonitor;
    }

    @Override
    public String getTaskId(SerialTask task) {
        return task.getId();
    }

    @Override
    public SerialTask getTaskById(String id) {
        if (id == null)
            return null;

        if (mCurrentExecuted != null && id.equals(getTaskId(mCurrentExecuted)))
            return mCurrentExecuted;

        for (SerialTask task : mTobeExecuted) {
            if (id.equals(getTaskId(task)))
                return task;
        }
        return null;
    }

    @Override
    public synchronized boolean addTask(SerialTask task) {
        if (getTaskById(getTaskId(task)) != null)// 判断是否重复
            return false;

        task.setSerialMgr(this);
        task.setStatus(TaskBean.STATUS_TODO);
        if (task.getSpeedCalculator() == null)
            task.setSpeedCalculator(new DefaultSpeedCalculator());
        mTobeExecuted.offer(task);
        for (SerialMgrListener<TaskBean> listener : mListeners)
            listener.onAdd(task.getBean());

        return true;
    }

    @Override
    public synchronized void addTasks(List<SerialTask> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<TaskBean> added = new ArrayList<TaskBean>();
        for (SerialTask task : tasks) {
            if (task == null)
                continue;
            if (getTaskById(getTaskId(task)) != null)// 判断是否重复
                continue;
            added.add(task.getBean());
            task.setSerialMgr(this);
            task.setStatus(TaskBean.STATUS_TODO);
            if (task.getSpeedCalculator() == null)
                task.setSpeedCalculator(new DefaultSpeedCalculator());
            mTobeExecuted.offer(task);
        }
        if (added.size() > 0)
            for (SerialMgrListener<TaskBean> listener : mListeners)
                listener.onAddAll(added);
    }

    @Override
    public synchronized void removeTask(SerialTask task) {
        if (task == null)
            return;

        boolean isRemoved;
        task.abort();// 终止当前任务
        if (mCurrentExecuted == task) {// 如果要删除的任务是当前的任务
            mCurrentExecuted = null;
            if (mIsWorking) // 如果当前状态是运行态，则暂停
                stop();
            isRemoved = true;
        } else {
            isRemoved = mTobeExecuted.remove(task);
        }
        if (isRemoved)
            for (SerialMgrListener<TaskBean> listener : mListeners)
                listener.onRemove(task.getBean());
    }

    @Override
    public synchronized void removeTaskById(String taskId) {
        removeTask(getTaskById(taskId));
    }

    @Override
    public synchronized void removeTasks(List<SerialTask> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<TaskBean> removed = new ArrayList<TaskBean>();
        for (SerialTask task : tasks) {
            if (task == null)
                continue;
            task.abort();// 终止当前任务
            if (mCurrentExecuted == task) {// 如果要删除的任务是当前的任务
                mCurrentExecuted = null;
                if (mIsWorking) // 如果当前状态是运行态，则暂停
                    stop();
                removed.add(task.getBean());
            } else {
                if (mTobeExecuted.remove(task))// 如果删除成功，添加进列表
                    removed.add(task.getBean());
            }
        }
        if (removed.size() > 0)
            for (SerialMgrListener<TaskBean> listener : mListeners)
                listener.onRemoveAll(removed);
    }

    @Override
    public synchronized void removeTasksById(List<String> taskIds) {
        if (taskIds == null || taskIds.size() == 0)
            return;

        List<SerialTask> tasks = new ArrayList<SerialTask>();
        for (String taskId : taskIds) {
            SerialTask task = getTaskById(taskId);
            if (task != null)
                tasks.add(task);
        }
        removeTasks(tasks);
    }

    @Override
    public synchronized void setRunningTask(String taskId) {
        SerialTask task = getTaskById(taskId);
        if (mCurrentExecuted == null && task != null) {
            mTobeExecuted.remove(task);
            mCurrentExecuted = task;
        }
    }

    @Override
    public SerialTask getRunningTask() {
        return mCurrentExecuted;
    }

    @Override
    public List<SerialTask> getWaitingTask() {
        return new ArrayList<SerialTask>(mTobeExecuted);
    }

    @Override
    public synchronized void start() {
        // 如果当前任务不为空，则恢复执行
        if (mCurrentExecuted != null) {
            resume();
        }
        // 否则，执行下一个任务
        else {
            if (mSpeedMonitor != null)
                mSpeedMonitor.stop();
            mCurrentExecuted = findNextTask();
            if (mCurrentExecuted != null) {
                mIsWorking = true;
                mCurrentExecuted.start();
                if (mSpeedMonitor != null)
                    mSpeedMonitor.start();
            }
        }
    }

    @Override
    public synchronized void start(String taskId) {
        SerialTask task = getTaskById(taskId);
        // 如果指定的task不存在，则调用start()
        if (task == null) {
            start();
            return;
        }

        mIsWorking = true;
        // 如果当前任务就是指定的任务，则继续任务
        if (mCurrentExecuted == task) {
            mCurrentExecuted.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        }
        // 如果不是，则先暂停当前任务，再执行指定任务
        else {
            if (mCurrentExecuted != null) {
                if (mSpeedMonitor != null)
                    mSpeedMonitor.stop();
                mCurrentExecuted.pause();
                // 添加回等待队列
                mTobeExecuted.addFirst(mCurrentExecuted);
            }
            // 执行指定的任务
            mTobeExecuted.remove(task);
            mCurrentExecuted = task;
            mCurrentExecuted.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        }
    }

    @Override
    public synchronized boolean resume() {
        if (mCurrentExecuted == null)
            return false;

        mIsWorking = true;
        mCurrentExecuted.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
        return true;
    }

    @Override
    public synchronized boolean resume(String taskId) {
        // 如果当前正在运行任务不为空，则恢复其运行
        if (resume())
            return true;

        // 如果当前没有正在运行任务，则尝试启动指定Id的任务
        SerialTask task = getTaskById(taskId);
        if (task == null)
            return false;
        // 执行指定的任务
        mIsWorking = true;
        mTobeExecuted.remove(task);
        mCurrentExecuted = task;
        mCurrentExecuted.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
        return true;
    }

    @Override
    public synchronized void pause() {
        mIsWorking = false;
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        if (mCurrentExecuted != null)
            mCurrentExecuted.pause();
    }

    @Override
    public synchronized boolean pause(String taskId) {
        if (TextUtils.isEmpty(taskId)) {
            pause();
            return true;
        }
        if (mCurrentExecuted == null ||
                !taskId.equals(mCurrentExecuted.getBean().getId()))
            return false;

        mIsWorking = false;
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mCurrentExecuted.pause();
        return true;
    }

    @Override
    public synchronized boolean pauseByType(int taskType) {
        if (mCurrentExecuted == null ||
                mCurrentExecuted.getBean().getType() != taskType)
            return false;

        mIsWorking = false;
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mCurrentExecuted.pause();
        return true;
    }

    @Override
    public synchronized void stop() {
        pause();
        if (mCurrentExecuted != null) {
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
        }
    }

    @Override
    public synchronized void stop(String taskId) {
        if (pause(taskId) && mCurrentExecuted != null) {
            // 添加回等待队列
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
        }
    }

    @Override
    public synchronized void stopByType(int taskType) {
        if (pauseByType(taskType) && mCurrentExecuted != null) {
            // 添加回等待队列
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
        }
    }

    @Override
    public synchronized void stopAndReset() {
        mIsWorking = false;
        // 停止速度监听
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        // 结束当前任务
        if (mCurrentExecuted != null) {
            mCurrentExecuted.abort();
            mCurrentExecuted = null;
        }
        // 结束并清空等待队列中的任务
        for (SerialTask task : mTobeExecuted)
            task.abort();
        mTobeExecuted.clear();
        // 通知监听者
        for (SerialMgrListener<TaskBean> listener : mListeners)
            listener.onStopAndReset();
    }

    @Override
    public void setTaskScheduler(TaskScheduler<TaskBean> scheduler) {
        mScheduler = scheduler;
    }

    /**
     * 寻找下一个任务。
     * @return 返回下一个要执行的任务，如果没有要执行的任务，则返回null
     * @see #setTaskScheduler(TaskScheduler)
     */
    protected SerialTask findNextTask() {
        // 用TaskScheduler排序
        if (mScheduler != null)
            Collections.sort(mTobeExecuted, mInnerComparator);

        return mTobeExecuted.poll();
    }

    @Override
    public synchronized void notifyTaskFinished(SerialTask task) {
        // 如果不是当前正在执行的任务
        if (task != mCurrentExecuted) {
            if (task.getStatus() == TaskBean.STATUS_TODO)
                mTobeExecuted.offer(task);// 如果是TODO状态添，加回等待队列
            else
                mTobeExecuted.remove(task);// 如果不是TODO状态，直接丢弃该任务
        }
        // 是当前正在执行的任务
        else {
            if (task.getStatus() == TaskBean.STATUS_DOING)// 非法状态
                return;
            // 如果是TODO结束的，添加到等待队列，并停止
            if (task.getStatus() == TaskBean.STATUS_TODO) {
                stop();
                return;
            }
            // 如果是ERROR结束的，或是DONE结束的，寻找下一个任务
            if (mSpeedMonitor != null)
                mSpeedMonitor.stop();
            mCurrentExecuted = findNextTask();
        }

        // 如果已经标记停止，则什么都不做
        if (!mIsWorking)
            return;

        if (mCurrentExecuted != null) {
            // 如果有任务，则继续执行任务
            mCurrentExecuted.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        } else {
            // 没有任务，标记结束
            mIsWorking = false;
        }
    }

    private int getIndexOfListener(String id) {
        if (id == null)
            return -1;

        for (int i = 0; i < mListeners.size(); i++)
            if (id.equals(mListeners.get(i).getId()))
                return i;
        return -1;
    }

    @Override
    public synchronized void registerListener(SerialMgrListener<TaskBean> listener) {
        if (listener == null)
            return;
        // 如果有相同id的listener，则先取消之前的listener
        unregisterListener(listener.getId());
        mListeners.add(listener);
    }

    @Override
    public synchronized void unregisterListener(String id) {
        int index = getIndexOfListener(id);
        if (index != -1)
            mListeners.remove(index);
    }

    @Override
    public List<SerialMgrListener<TaskBean>> getListeners() {
        return mListeners;
    }

    /**
     * 内部Comparator<T>子类，用于对mTobeExecuted进行优先级排序。
     * 通过传入的TaskScheduler来实际进行排序比较。
     */
    private class InnerTaskComparator implements Comparator<SerialTask> {
        @Override
        public int compare(SerialTask lhs, SerialTask rhs) {
            return mScheduler.compare(lhs.getBean(), rhs.getBean(),
                    mCurrentExecuted == null ? null : mCurrentExecuted.getBean());
        }
    }
}
