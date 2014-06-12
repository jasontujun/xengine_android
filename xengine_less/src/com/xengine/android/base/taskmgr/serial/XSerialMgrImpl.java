package com.xengine.android.base.taskmgr.serial;

import com.xengine.android.base.filter.XFilter;
import com.xengine.android.base.listener.XCowListenerMgr;
import com.xengine.android.base.listener.XListenerMgr;
import com.xengine.android.base.speed.XSpeedMonitor;
import com.xengine.android.base.speed.calc.DefaultSpeedCalculator;
import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.taskmgr.XMgrTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgrListener;
import com.xengine.android.base.taskmgr.XTaskScheduler;

import java.util.*;

/**
 * <pre>
 * 线性执行器，实现SerialMgr接口的子类。
 * 封装了线性执行、增删任务、启动、恢复、暂停、速度监视等操作。
 * 1.每个时刻，最多只有一个任务正在执行；
 * 2.每个任务都在TODO,DOING,DONE,ERROR四个状态间转换；
 * 3.如果任务从DOING到ERROR，回调SerialMgr时，
 *   会将该任务丢弃，继续执行下一个；
 * 4.如果任务从DOING到TODO，回调SerialMgr时，
 *   会将该任务重新添加进等待队列，不继续执行；
 * @see com.xengine.android.base.task.XTaskExecutor
 * User: tujun
 * Date: 13-8-1
 * Time: 下午3:35
 * </pre>
 */
public class XSerialMgrImpl<B extends XTaskBean>
        implements XSerialMgr<XMgrTaskExecutor<B>, B> {

    private boolean mIsWorking;// 标识运行状态
    private volatile XMgrTaskExecutor<B> mCurrentExecuted;// 当前正在运行的任务
    private volatile LinkedList<XMgrTaskExecutor<B>> mTobeExecuted;// 待执行的任务队列
    private XTaskScheduler<B> mScheduler;// 任务排序器(外部设置)
    private XFilter<B> mFilter;// 任务过滤器
    private Comparator<XMgrTaskExecutor<B>> mInnerComparator;// 实际用来排序的比较器
    private XSpeedMonitor<XMgrTaskExecutor<B>> mSpeedMonitor;// 速度监视器
    private XListenerMgr<XTaskMgrListener<B>> mListeners;// 外部监听者

    public XSerialMgrImpl() {
        mCurrentExecuted = null;
        mTobeExecuted = new LinkedList<XMgrTaskExecutor<B>>();
        mInnerComparator = new InnerTaskComparator();
        mListeners = new XCowListenerMgr<XTaskMgrListener<B>>();
        mIsWorking = false;
    }

    @Override
    public String getTaskId(XMgrTaskExecutor<B> task) {
        return task.getId();
    }

    @Override
    public XMgrTaskExecutor<B> getTaskById(String id) {
        if (id == null)
            return null;

        if (mCurrentExecuted != null && id.equals(getTaskId(mCurrentExecuted)))
            return mCurrentExecuted;

        for (XMgrTaskExecutor<B> task : mTobeExecuted) {
            if (id.equals(getTaskId(task)))
                return task;
        }
        return null;
    }

    @Override
    public synchronized boolean addTask(XMgrTaskExecutor<B> task) {
        if (getTaskById(getTaskId(task)) != null)// 判断是否重复
            return false;

        task.setTaskMgr(this);
        task.setStatus(XTaskBean.STATUS_TODO);
        if (task.getSpeedCalculator() == null)
            task.setSpeedCalculator(new DefaultSpeedCalculator());
        mTobeExecuted.offer(task);
        for (XTaskMgrListener<B> listener : mListeners.getListeners())
            listener.onAdd(task.getBean());

        return true;
    }

    @Override
    public synchronized void addTasks(List<XMgrTaskExecutor<B>> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<B> added = new ArrayList<B>();
        for (XMgrTaskExecutor<B> task : tasks) {
            if (task == null)
                continue;
            if (getTaskById(getTaskId(task)) != null)// 判断是否重复
                continue;
            added.add(task.getBean());
            task.setTaskMgr(this);
            task.setStatus(XTaskBean.STATUS_TODO);
            if (task.getSpeedCalculator() == null)
                task.setSpeedCalculator(new DefaultSpeedCalculator());
            mTobeExecuted.offer(task);
        }
        if (added.size() > 0)
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onAddAll(added);
    }

    @Override
    public synchronized void removeTask(XMgrTaskExecutor<B> task) {
        if (task == null)
            return;

        boolean isRemoved;
        task.abort();// 终止当前任务
        if (mCurrentExecuted == task) {// 如果要删除的任务是当前的任务
            mCurrentExecuted = null;
            isRemoved = true;
        } else {
            isRemoved = mTobeExecuted.remove(task);
        }
        if (mCurrentExecuted == null) {// 如果当前没有任务运行，则标记结束
            if (mSpeedMonitor != null)
                mSpeedMonitor.stop();
            mIsWorking = false;
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
        if (isRemoved)
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onRemove(task.getBean());
    }

    @Override
    public synchronized void removeTaskById(String taskId) {
        removeTask(getTaskById(taskId));
    }

    @Override
    public synchronized void removeTasks(List<XMgrTaskExecutor<B>> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<B> removed = new ArrayList<B>();
        for (XMgrTaskExecutor<B> task : tasks) {
            if (task == null)
                continue;
            task.abort();// 终止当前任务
            if (mCurrentExecuted == task) {// 如果要删除的任务是当前的任务
                mCurrentExecuted = null;
                removed.add(task.getBean());
            } else {
                if (mTobeExecuted.remove(task))// 如果删除成功，添加进列表
                    removed.add(task.getBean());
            }
        }
        if (mCurrentExecuted == null) {// 如果当前没有任务运行，则标记结束
            if (mSpeedMonitor != null)
                mSpeedMonitor.stop();
            mIsWorking = false;
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
        if (removed.size() > 0)
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onRemoveAll(removed);
    }

    @Override
    public synchronized void removeTasksById(List<String> taskIds) {
        if (taskIds == null || taskIds.size() == 0)
            return;

        List<XMgrTaskExecutor<B>> tasks = new ArrayList<XMgrTaskExecutor<B>>();
        for (String taskId : taskIds) {
            XMgrTaskExecutor<B> task = getTaskById(taskId);
            if (task != null)
                tasks.add(task);
        }
        removeTasks(tasks);
    }

    @Override
    public synchronized void setRunningTask(String taskId) {
        XMgrTaskExecutor<B> task = getTaskById(taskId);
        if (mCurrentExecuted == null && task != null) {
            mTobeExecuted.remove(task);
            mCurrentExecuted = task;
        }
    }

    @Override
    public XMgrTaskExecutor<B> getRunningTask() {
        return mCurrentExecuted;
    }

    @Override
    public List<XMgrTaskExecutor<B>> getWaitingTask() {
//        return new ArrayList<XMgrTaskExecutor>(mTobeExecuted);
        // 为了效率起见，牺牲安全性
        return mTobeExecuted;
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
            // 下一个任务不为空，且没被过滤掉，则启动
            if (mCurrentExecuted != null &&
                    (mFilter == null || mFilter.doFilter(mCurrentExecuted.getBean()) != null)) {
                mIsWorking = true;
                mCurrentExecuted.start();
                if (mSpeedMonitor != null)
                    mSpeedMonitor.start();
            }
        }
    }

    @Override
    public synchronized boolean start(String taskId) {
        XMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定的task不存在，则什么都不做，返回false
        if (task == null)
            return false;

        // 如果被过滤掉，则不启动
        if (mFilter != null && mFilter.doFilter(task.getBean()) == null)
            return false;

        mIsWorking = true;
        // 如果当前任务不是指定id任务，先暂停当前任务，再指定id的为当前任务
        if (mCurrentExecuted != task) {
            // 暂停老的当前任务
            if (mCurrentExecuted != null) {
                if (mSpeedMonitor != null)
                    mSpeedMonitor.stop();
                mCurrentExecuted.pause();
                // 添加回等待队列
                mTobeExecuted.addFirst(mCurrentExecuted);
            }
            // 指定新的当前任务
            mTobeExecuted.remove(task);
            mCurrentExecuted = task;
        }
        mCurrentExecuted.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
        return true;
    }

    @Override
    public synchronized void resume() {
        if (mCurrentExecuted == null)
            return;

        // 如果被过滤掉，则不启动
        if (mFilter != null && mFilter.doFilter(mCurrentExecuted.getBean()) == null)
            return;

        mIsWorking = true;
        mCurrentExecuted.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
    }

    @Override
    public synchronized boolean resume(String taskId) {
        XMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定的task不存在，则什么都不做，返回false
        if (task == null)
            return false;

        // 如果被过滤掉，则不启动
        if (mFilter != null && mFilter.doFilter(task.getBean()) == null)
            return false;

        // 如果指定Id的任务存在，且在运行队列中，恢复该任务
        if (mCurrentExecuted == task) {
            mIsWorking = true;
            mCurrentExecuted.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
            return true;
        }

        // 如果指定Id的任务存在，且在等待队列中，运行队列已满，什么都不做
        if (mCurrentExecuted != null)
            return false;

        // 如果指定Id的任务存在，且在等待队列中，且运行队列未满，启动该任务
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
        if (mCurrentExecuted != null)
            mCurrentExecuted.pause();
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mIsWorking = false;
    }

    @Override
    public synchronized boolean pause(String taskId) {
        XMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定Id的任务不存在，或在等待队列中，则什么都不做，返回false
        if (task == null || mCurrentExecuted != task)
            return false;
        // 如果指定Id的任务存在，且在运行队列中，暂停该任务
        mCurrentExecuted.pause();
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mIsWorking = false;
        return true;
    }

    @Override
    public synchronized boolean pauseByFilter(XFilter<B> filter) {
        setTaskFilter(filter);// 设置当前的任务过滤器
        if (mCurrentExecuted == null || filter == null ||
                filter.doFilter(mCurrentExecuted.getBean()) != null)
            return false;

        mCurrentExecuted.pause();
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mIsWorking = false;

        return true;
    }

    @Override
    public synchronized void stop() {
        pause();
        if (mCurrentExecuted != null) {
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
    }

    @Override
    public synchronized boolean stop(String taskId) {
        if (pause(taskId) && mCurrentExecuted != null) {
            // 添加回等待队列
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean stopByFilter(XFilter<B> filter) {
        if (pauseByFilter(filter) && mCurrentExecuted != null) {
            // 添加回等待队列
            mTobeExecuted.addFirst(mCurrentExecuted);
            mCurrentExecuted = null;
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
            return true;
        }
        return false;
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
        for (XMgrTaskExecutor<B> task : mTobeExecuted)
            task.abort();
        mTobeExecuted.clear();
        // 通知监听者
        for (XTaskMgrListener<B> listener : mListeners.getListeners())
            listener.onStopAndReset();
    }

    @Override
    public void setSpeedMonitor(XSpeedMonitor<XMgrTaskExecutor<B>> speedMonitor) {
        mSpeedMonitor = speedMonitor;
    }

    @Override
    public void setTaskFilter(XFilter<B> filter) {
        mFilter = filter;
    }

    @Override
    public void setTaskScheduler(XTaskScheduler<B> scheduler) {
        mScheduler = scheduler;
    }

    /**
     * 寻找下一个任务。
     * 策略：1.将任务排序，过滤，返回第一个是TODO状态的任务(其他状态的任务忽略)
     *       2.如果没有符合1要求的任务，则返回第一个TODO状态但被过滤的任务
     *       3.如果没有以上的任务，则返回null
     * @return 返回下一个待执行的任务，如果没有符合要求的任务，则返回null
     * @see #setTaskScheduler(com.xengine.android.base.taskmgr.XTaskScheduler)
     */
    protected XMgrTaskExecutor<B> findNextTask() {
        // 用TaskScheduler排序
        if (mScheduler != null)
            Collections.sort(mTobeExecuted, mInnerComparator);

        // 用TaskFilter过滤，找到第一个是TODO状态的任务
        final XFilter<B> finalFilter = mFilter;
        XMgrTaskExecutor<B> nextTask = null;// 最终的结果，下一个待执行任务
        XMgrTaskExecutor<B> filteredTask = null;// 第一个符合状态但被过滤掉的任务
        for (XMgrTaskExecutor<B> task : mTobeExecuted) {
            // 不是TODO状态的任务，跳过
            if (task.getStatus() != XTaskBean.STATUS_TODO)
                continue;
            // TODO状态，且没被过滤掉的任务
            if ((finalFilter == null || finalFilter.doFilter(task.getBean()) != null)) {
                nextTask = task;
                break;
            }
            // TODO状态，但被过滤掉的任务
            else {
                if (filteredTask == null)
                    filteredTask = task;
            }
        }
        // 如果没有符合的任务，则返回第一个状态正确但被过滤的任务
        if (nextTask == null) {
            nextTask = filteredTask;
        }
        // 如果找到下一个任务，则将其从等待队列中移除
        if (nextTask != null)
            mTobeExecuted.remove(nextTask);
        return nextTask;
    }

    @Override
    public synchronized void notifyTaskFinished(XMgrTaskExecutor<B> task, boolean addBack) {
        if (task == null)
            return;

        // 如果不是当前正在执行的任务（可能是没执行就被外部pause或abort了）
        if (task != mCurrentExecuted) {
            // 如果是TODO状态添，且addBack为true，才能加回等待队列
            if (addBack && task.getStatus() == XTaskBean.STATUS_TODO) {
                if (!mTobeExecuted.contains(task))
                    mTobeExecuted.offer(task);
            } else {
                mTobeExecuted.remove(task);// 否则，直接丢弃该任务
            }
            return;
        }

        // 是当前正在执行的任务
        if (task.getStatus() == XTaskBean.STATUS_DOING)// 正在执行,非法状态
            return;

        // 如果是TODO结束的，ERROR结束的，或是DONE结束的，寻找下一个任务
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mCurrentExecuted = findNextTask();
        // 下一个任务为空，但当前等待队列不为空，则说明等待队列中所有的任务都是异常状态
        boolean allError = (mCurrentExecuted == null && mTobeExecuted.size() > 0);
        // 如果是TODO或ERROR结束的，且addBack为true，添加回等待队列
        if (addBack && task.getStatus() != XTaskBean.STATUS_DONE
                && !mTobeExecuted.contains(task))
            mTobeExecuted.offer(task);
        // 如果等待队列中所有的任务都是异常状态，则全部重置成TODO，方便下次全部自动执行
        if (allError) {
            for (XMgrTaskExecutor<B> errorTask : mTobeExecuted)
                errorTask.setStatus(XTaskBean.STATUS_TODO);
        }

        // 如果已经标记停止，则什么都不做
        if (!mIsWorking)
            return;

        // 如果有任务(没被过滤)，则继续执行任务
        if (mCurrentExecuted != null &&
                (mFilter == null || mFilter.doFilter(mCurrentExecuted.getBean()) != null)) {
            mCurrentExecuted.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        }
        // 没有任务，标记结束
        else {
            mIsWorking = false;
            // 当前没有执行任务，等待队列也没任务，则回调onFinishAll()
            if (mTobeExecuted.size() == 0)
                for (XTaskMgrListener<B> listener : mListeners.getListeners())
                    listener.onFinishAll();
        }
    }

    @Override
    public void registerListener(XTaskMgrListener<B> listener) {
        mListeners.registerListener(listener);
    }

    @Override
    public void unregisterListener(XTaskMgrListener<B> listener) {
        mListeners.unregisterListener(listener);
    }

    @Override
    public List<XTaskMgrListener<B>> getListeners() {
        return mListeners.getListeners();
    }

    /**
     * 内部Comparator<T>子类，用于对mTobeExecuted进行优先级排序。
     * 通过传入的TaskScheduler来实际进行排序比较。
     */
    private class InnerTaskComparator implements Comparator<XMgrTaskExecutor<B>> {
        @Override
        public int compare(XMgrTaskExecutor<B> lhs, XMgrTaskExecutor<B> rhs) {
            return mScheduler.compare(lhs.getBean(), rhs.getBean(),
                    mCurrentExecuted == null ? null : mCurrentExecuted.getBean());
        }
    }
}
