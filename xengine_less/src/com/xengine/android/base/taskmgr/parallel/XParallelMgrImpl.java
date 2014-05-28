package com.xengine.android.base.taskmgr.parallel;

import com.xengine.android.base.filter.XFilter;
import com.xengine.android.base.listener.XCowListenerMgr;
import com.xengine.android.base.listener.XListenerMgr;
import com.xengine.android.base.speed.XSpeedMonitor;
import com.xengine.android.base.speed.calc.DefaultSpeedCalculator;
import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.taskmgr.XBaseMgrTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgrListener;
import com.xengine.android.base.taskmgr.XTaskScheduler;

import java.util.*;

/**
 * <pre>
 * 并行执行器，实现ParallelMgr接口的子类。
 * 封装了并行执行、增删任务、启动、恢复、暂停、速度监视等操作。
 * 1.每个时刻，有一个或多个(不超过设置上限)任务正在执行；
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
public class XParallelMgrImpl<B extends XTaskBean>
        implements XParallelMgr<XBaseMgrTaskExecutor<B>, B> {

    private boolean mIsWorking;// 标识运行状态
    private int mParallelLimit;// 并行任务的数量上限
    private LinkedList<XBaseMgrTaskExecutor<B>> mCurrentExecuted;// 正在运行的任务队列
    private LinkedList<XBaseMgrTaskExecutor<B>> mTobeExecuted;// 待执行的任务队列
    private XTaskScheduler<B> mScheduler;// 任务排序器(外部设置)
    private XFilter<B> mFilter;// 任务过滤器
    private InnerTaskComparator mInnerComparator;// 实际用来排序的比较器
    private XSpeedMonitor<XBaseMgrTaskExecutor<B>> mSpeedMonitor;// 下载速度监视器
    private XListenerMgr<XTaskMgrListener<B>> mListeners;// 外部监听者

    public XParallelMgrImpl(int parallelLimit) {
        mParallelLimit = Math.max(parallelLimit, 1);
        mCurrentExecuted = new LinkedList<XBaseMgrTaskExecutor<B>>();
        mTobeExecuted = new LinkedList<XBaseMgrTaskExecutor<B>>();
        mInnerComparator = new InnerTaskComparator();
        mListeners = new XCowListenerMgr<XTaskMgrListener<B>>();
        mIsWorking = false;
    }

    /**
     * 设置速度监控器
     * @param speedMonitor
     */
    public void setSpeedMonitor(XSpeedMonitor<XBaseMgrTaskExecutor<B>> speedMonitor) {
        mSpeedMonitor = speedMonitor;
    }

    @Override
    public boolean isEmptyParallel() {
        return mCurrentExecuted.size() == 0;
    }

    @Override
    public boolean isFullParallel() {
        return mCurrentExecuted.size() >= mParallelLimit;
    }

    @Override
    public boolean isAllStop() {
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted) {
            if (task.getStatus() == XTaskBean.STATUS_DOING)
                return false;
        }
        return true;
    }

    /**
     * 如果所有任务都是暂停状态，
     * 则设置运行标识为false，停止速度监视器
     * @return 如果所有任务都是暂停状态，则返回true;否则返回false
     */
    private boolean setStopIfAllStop() {
        if (!isAllStop())
            return false;

        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mIsWorking = false;
        return true;
    }

    @Override
    public String getTaskId(XBaseMgrTaskExecutor<B> task) {
        return task.getId();
    }

    @Override
    public XBaseMgrTaskExecutor<B> getTaskById(String id) {
        if (id == null)
            return null;

        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted) {
            if (id.equals(getTaskId(task)))
                return task;
        }
        for (XBaseMgrTaskExecutor<B> task : mTobeExecuted) {
            if (id.equals(getTaskId(task)))
                return task;
        }
        return null;
    }

    @Override
    public synchronized boolean addTask(XBaseMgrTaskExecutor<B> task) {
        if (getTaskById(getTaskId(task)) != null)// 判断是否重复
            return false;

        task.setTaskMgr(this);
        task.setStatus(XTaskBean.STATUS_TODO);
        if (task.getSpeedCalculator() == null) // 若没有速度计算器，则设置默认的
            task.setSpeedCalculator(new DefaultSpeedCalculator());
        mTobeExecuted.offer(task);
        for (XTaskMgrListener<B> listener : mListeners.getListeners())
            listener.onAdd(task.getBean());

        return true;
    }

    @Override
    public synchronized void addTasks(List<XBaseMgrTaskExecutor<B>> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<B> added = new ArrayList<B>();
        for (XBaseMgrTaskExecutor<B> task : tasks) {
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
    public synchronized void removeTask(XBaseMgrTaskExecutor<B> task) {
        if (task == null)
            return;

        task.abort();// 终止当前任务
        boolean isRemoved;
        if (mCurrentExecuted.contains(task)) {// 如果要删除的任务在执行队列中
            isRemoved = mCurrentExecuted.remove(task);
        } else {
            isRemoved = mTobeExecuted.remove(task);
        }
        setStopIfAllStop();// 如果当前没有任务运行，则标记结束
        if (!mIsWorking) {
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
    public synchronized void removeTasks(List<XBaseMgrTaskExecutor<B>> tasks) {
        if (tasks == null || tasks.size() == 0)
            return;

        List<B> removed = new ArrayList<B>();
        for (XBaseMgrTaskExecutor<B> task : tasks) {
            if (task == null)
                continue;
            task.abort();// 终止任务
            if (mCurrentExecuted.contains(task)) {// 如果要删除的任务在执行队列中
                mCurrentExecuted.remove(task);
                removed.add(task.getBean());
            } else {
                if (mTobeExecuted.remove(task))// 如果删除成功，添加进列表
                    removed.add(task.getBean());
            }
        }
        setStopIfAllStop();// 如果当前没有任务运行，则标记结束
        if (!mIsWorking) {
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

        List<XBaseMgrTaskExecutor<B>> tasks = new ArrayList<XBaseMgrTaskExecutor<B>>();
        for (String taskId : taskIds) {
            XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
            if (task != null)
                tasks.add(task);
        }
        removeTasks(tasks);
    }

    @Override
    public synchronized void setRunningTask(String taskId) {
        // 如果运行队列已满，则什么都不做
        if (isFullParallel())
            return;

        // 如果运行队列未满，则将指定任务从等待队列添加进运行队列
        XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
        if (task != null) {
            mTobeExecuted.remove(task);
            mCurrentExecuted.addLast(task);
        }
    }

    @Override
    public List<XBaseMgrTaskExecutor<B>> getRunningTask() {
//        return new ArrayList<ParallelTask>(mCurrentExecuted);
        // 为了效率起见，牺牲安全性
        return mCurrentExecuted;
    }

    @Override
    public List<XBaseMgrTaskExecutor<B>> getWaitingTask() {
//        return new ArrayList<ParallelTask>(mTobeExecuted);
        // 为了效率起见，牺牲安全性
        return mTobeExecuted;
    }

    @Override
    public synchronized void start() {
        // 启动运行队列的所有任务
        resume();
        // 如果运行队列已满，则什么都不做
        if (isFullParallel())
            return;
        // 如果运行队列未满，则启动多个等待队列中的任务直到满
        XBaseMgrTaskExecutor<B> task;
        while (!isFullParallel()) {
            task = findNextTask(null);
            if (task == null)
                break;
            // 如果下一个任务是被过滤掉的，说明已经没有可执行的任务了，退出循环
            if (mFilter != null && mFilter.doFilter(task.getBean()) == null)
                break;
            mIsWorking = true;
            mCurrentExecuted.offer(task);
            task.start();
        }
        if (mIsWorking && mSpeedMonitor != null)
            mSpeedMonitor.start();
    }

    @Override
    public synchronized boolean start(String taskId) {
        XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定Id的任务不存在，则什么都不做，返回false
        if (task == null)
            return false;

        // 如果被过滤掉，则不启动
        if (mFilter != null && mFilter.doFilter(task.getBean()) == null)
            return false;

        mIsWorking = true;
        // 如果指定Id的任务存在，且在等待队列中，则添加进运行队列
        if (!mCurrentExecuted.contains(task)) {
            // 如果运行队列已满，则替换一个任务
            if (isFullParallel()) {// 如果运行队列已满，则剔除最老的任务
                XBaseMgrTaskExecutor<B> oldTask = mCurrentExecuted.poll();
                oldTask.pause();
                mTobeExecuted.addFirst(oldTask);// 添加回等待队列
            }
            // 启动指定的任务
            mTobeExecuted.remove(task);
            mCurrentExecuted.addFirst(task);
            task.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        }
        // 指定Id的任务在运行队列中，启动该任务
        task.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
        return true;
    }

    @Override
    public synchronized void resume() {
        if (isEmptyParallel())
            return;

        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted) {
            // 如果没被过滤掉，则启动
            if (mFilter == null || mFilter.doFilter(task.getBean()) != null) {
                mIsWorking = true;
                task.start();
            }
        }
        if (mIsWorking && mSpeedMonitor != null)
            mSpeedMonitor.start();
    }

    @Override
    public synchronized boolean resume(String taskId) {
        XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定的task不存在，则什么都不做，返回false
        if (task == null)
            return false;

        // 如果被过滤掉，则不启动
        if (mFilter != null && mFilter.doFilter(task.getBean()) == null)
            return false;

        // 如果指定Id的任务存在，且在运行队列中，恢复该任务
        if (mCurrentExecuted.contains(task)) {
            mIsWorking = true;
            task.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
            return true;
        }

        // 如果指定Id的任务存在，且在等待队列中，运行队列已满，什么都不做
        if (isFullParallel())
            return false;

        // 如果指定Id的任务存在，且在等待队列中，且运行队列未满，启动该任务
        mIsWorking = true;
        mTobeExecuted.remove(task);
        mCurrentExecuted.offer(task);
        task.start();
        if (mSpeedMonitor != null)
            mSpeedMonitor.start();
        return true;
    }

    @Override
    public synchronized void pause() {
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted)
            task.pause();
        mIsWorking = false;
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
    }

    @Override
    public synchronized boolean pause(String taskId) {
        XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定Id的任务不存在，或在等待队列中，则什么都不做，返回false
        if (task == null || !mCurrentExecuted.contains(task))
            return false;
        // 如果指定Id的任务存在，且在运行队列中，暂停该任务
        task.pause();
        setStopIfAllStop();
        return true;
    }

    @Override
    public synchronized boolean pauseByFilter(XFilter<B> filter) {
        setTaskFilter(filter);// 设置当前的任务过滤器
        boolean result = false;
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted) {
            if (filter != null && filter.doFilter(task.getBean()) == null) {
                task.pause();
                result = true;
            }
        }
        setStopIfAllStop();
        return result;
    }

    @Override
    public synchronized void stop() {
        pause();
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted)
            mTobeExecuted.addFirst(task);
        mCurrentExecuted.clear();
        if (!mIsWorking) {
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
    }

    @Override
    public synchronized boolean stop(String taskId) {
        XBaseMgrTaskExecutor<B> task = getTaskById(taskId);
        // 如果指定Id的任务不存在，或在等待队列中，则什么都不做，返回false
        if (task == null || !mCurrentExecuted.contains(task))
            return false;
        // 如果指定Id的任务存在，且在运行队列中，暂停该任务(将任务从运行队列移回等待队列)
        task.pause();
        mCurrentExecuted.remove(task);
        mTobeExecuted.addFirst(task);
        setStopIfAllStop();
        if (!mIsWorking) {
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
        return true;
    }

    @Override
    public synchronized boolean stopByFilter(XFilter<B> filter) {
        setTaskFilter(filter);// 设置当前的任务过滤器
        boolean result = false;
        List<XBaseMgrTaskExecutor<B>> tmpList = new ArrayList<XBaseMgrTaskExecutor<B>>();
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted) {
            if (filter != null && filter.doFilter(task.getBean()) == null) {
                task.pause();
                result = true;
                tmpList.add(task);
            }
        }
        if (!result)
            return false;// 没有要暂停的

        mCurrentExecuted.removeAll(tmpList);
        mTobeExecuted.addAll(0, tmpList);
        setStopIfAllStop();
        if (!mIsWorking) {
            for (XTaskMgrListener<B> listener : mListeners.getListeners())
                listener.onStopAll();
        }
        return true;
    }

    @Override
    public synchronized void stopAndReset() {
        mIsWorking = false;
        // 终止并清空当前任务
        for (XBaseMgrTaskExecutor<B> task : mCurrentExecuted)
            task.abort();
        mCurrentExecuted.clear();
        // 终止并清空等待队列中的任务
        for (XBaseMgrTaskExecutor<B> task : mTobeExecuted)
            task.abort();
        mTobeExecuted.clear();
        // 停止速度监听
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        // 通知监听者
        for (XTaskMgrListener<B> listener : mListeners.getListeners())
            listener.onStopAndReset();
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
     * @return 返回下一个待执行的任务，如果没有可执行的任务，则返回null
     * @see #setTaskScheduler(com.xengine.android.base.taskmgr.XTaskScheduler)
     */
    protected XBaseMgrTaskExecutor<B> findNextTask(XBaseMgrTaskExecutor<B> curTask) {
        // 用TaskScheduler排序
        if (mScheduler != null) {
            mInnerComparator.setCurTask(curTask);
            Collections.sort(mTobeExecuted, mInnerComparator);
        }

        // 用TaskFilter过滤，找到第一个是TODO状态的任务
        final XFilter<B> finalFilter = mFilter;
        XBaseMgrTaskExecutor<B> nextTask = null;// 最终的结果，下一个待执行任务
        XBaseMgrTaskExecutor<B> filteredTask = null;// 第一个符合状态但被过滤掉的任务
        for (XBaseMgrTaskExecutor<B> task : mTobeExecuted) {
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
    public synchronized void notifyTaskFinished(XBaseMgrTaskExecutor<B> task, boolean addBack) {
        if (task == null)
            return;

        // 如果不是在执行队列中的任务（可能是没执行就被外部pause或abort了）
        if (!mCurrentExecuted.contains(task)) {
            // 如果是TODO状态添，且addBack为true，才能加回等待队列
            if (addBack && task.getStatus() == XTaskBean.STATUS_TODO) {
                if (!mTobeExecuted.contains(task))
                    mTobeExecuted.offer(task);
            } else {
                mTobeExecuted.remove(task);// 否则，直接丢弃该任务
            }
            return;
        }

        // 是执行队列中的的任务
        if (task.getStatus() == XTaskBean.STATUS_DOING)// 正在执行,非法状态
            return;

        // 如果是TODO结束的，ERROR结束的，或是DONE结束的，寻找下一个任务
        if (mSpeedMonitor != null)
            mSpeedMonitor.stop();
        mCurrentExecuted.remove(task);
        XBaseMgrTaskExecutor<B> nextTask = findNextTask(task);
        if (nextTask != null)
            mCurrentExecuted.offer(nextTask);
        // 下一个任务为空，但当前等待队列不为空，则说明等待队列中所有的任务都是异常状态
        boolean allError = (nextTask == null && mTobeExecuted.size() > 0);
        // 如果是TODO或ERROR结束的，且addBack为true，添加回等待队列
        if (addBack && task.getStatus() != XTaskBean.STATUS_DONE
                && !mTobeExecuted.contains(task))
            mTobeExecuted.offer(task);
        // 如果等待队列中所有的任务都是异常状态，则全部重置成TODO，方便下次全部自动执行
        if (allError) {
            for (XBaseMgrTaskExecutor<B> errorTask : mTobeExecuted)
                errorTask.setStatus(XTaskBean.STATUS_TODO);
        }

        // 如果已经标记停止，则什么都不做
        if (!mIsWorking)
            return;

        // 如果有任务(没被过滤)，则继续执行任务
        if (nextTask != null &&
                (mFilter == null || mFilter.doFilter(nextTask.getBean()) != null)) {
            nextTask.start();
            if (mSpeedMonitor != null)
                mSpeedMonitor.start();
        }
        // 没有下一个任务，且没有运行的任务，标记结束
        else if (setStopIfAllStop()){
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
    private class InnerTaskComparator implements Comparator<XBaseMgrTaskExecutor<B>> {

        private XBaseMgrTaskExecutor<B> curTask;

        public void setCurTask(XBaseMgrTaskExecutor<B> curTask) {
            this.curTask = curTask;
        }

        @Override
        public int compare(XBaseMgrTaskExecutor<B> lhs, XBaseMgrTaskExecutor<B> rhs) {
            return mScheduler.compare(lhs.getBean(), rhs.getBean(),
                    mCurrentExecuted == null ? null : curTask.getBean());
        }
    }
}
