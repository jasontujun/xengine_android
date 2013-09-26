package com.xengine.android.system.series;

/**
 * 线性任务执行的接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 */
public interface XSerial<T> {

    /**
     * 添加任务(不重复)。
     * @param task
     * @return 如果任务已存在，则返回false，添加失败；否则返回true。
     */
    boolean addNewTask(T task);

    /**
     * 删除任务。
     * 如果任务是当前正在执行的任务，则终止并执行下一个任务
     * @see #findNextTask()
     * @param task
     */
    void removeTask(T task);

    /**
     * 开始任务队列的执行。
     */
    void start();

    /**
     * 暂停任务队列的执行。
     */
    void stop();

    /**
     * 停止并清空任务队列。
     */
    void stopAndReset();

    /**
     * 寻找下一个任务。
     * @return
     */
    T findNextTask();
}
