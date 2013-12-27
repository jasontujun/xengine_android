package com.xengine.android.unknow.serial;

import tv.pps.module.download.core.task.TaskBean;
import tv.pps.module.download.core.task.TaskExecutor;

import java.util.List;

/**
 * <pre>
 * 线性任务执行器的接口。
 * T表示任务的类型
 * B表示数据的类型
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 * </pre>
 */
public interface SerialMgr<T extends TaskExecutor, B extends TaskBean> {

    /**
     * 获取task的唯一Id。
     * @param task 任务
     * @return 如果返回的id为null，则认为此task唯一。
     */
    String getTaskId(T task);

    /**
     * 根据task的唯一Id获取队列中的任务。
     * @param taskId  任务的唯一Id
     * @return 返回指定Id的任务，如果不存在，则返回null
     */
    T getTaskById(String taskId);

    /**
     * 添加任务(不重复，根据taskId判断唯一性)。
     * @param task 任务
     * @return 如果任务已存在，则返回false，添加失败；否则返回true。
     */
    boolean addTask(T task);

    /**
     * 批量添加任务(不重复，根据taskId判断唯一性)。
     * @param tasks 任务
     */
    void addTasks(List<T> tasks);

    /**
     * 删除任务(传入任务的对象)。
     * 如果任务是当前正在执行的任务，则终止并执行下一个任务
     * @param task 要删除的任务
     */
    void removeTask(T task);

    /**
     * 删除任务(传入任务的id)。
     * 如果任务是当前正在执行的任务，则终止并整体暂停
     * @param taskId 任务的唯一Id
     */
    void removeTaskById(String taskId);

    /**
     * 批量删除任务(传入任务对象列表)
     * @param tasks  待删除的任务对象列表
     */
    void removeTasks(List<T> tasks);

    /**
     * 批量删除任务(传入任务id列表)
     * @param taskIds 待删除的任务Id列表
     */
    void removeTasksById(List<String> taskIds);

    /**
     * 如果当前运行任务为空，则将指定任务从等待队列中移除，
     * 并设置为当前运行任务，不会启动执行。
     * 如果当前运行任务不为空，则什么都不做。
     * @param taskId 任务的唯一Id
     */
    void setRunningTask(String taskId);

    /**
     * 获取当前正在运行的任务
     * @return 返回当前正在运行的任务，如果没有，则返回null
     */
    T getRunningTask();

    /**
     * 获取当前等待队列的所有任务
     * @return 返回等待执行的任务列表
     */
    List<T> getWaitingTask();

    /**
     * 如果当前正在运行任务不为空，则恢复其运行；
     * 如果当前没有正在运行任务，则启动一个任务运行
     * @see #resume()
     */
    void start();

    /**
     * 尝试启动指定Id的任务(并停止上一个任务)；
     * 如果指定Id的任务不存在，则逻辑同start()
     * @param taskId 任务的唯一Id
     * @see #start()
     */
    void start(String taskId);

    /**
     * 如果当前正在运行任务不为空，则恢复其运行；
     * 如果当前没有正在运行任务，则什么都不做
     * @return 如果恢复成功，则返回true；否则返回false
     * @see #start()
     */
    boolean resume();

    /**
     * 如果当前正在运行任务不为空，则恢复其运行；
     * 如果当前没有正在运行任务，则尝试启动指定Id的任务
     * @param taskId 任务的唯一Id
     * @return 如果恢复成功，则返回true；否则返回false
     */
    boolean resume(String taskId);

    /**
     * 尝试暂停当前任务(不会将当前任务置为空)。
     * 如果当前没有任务在运行，则什么都不做
     */
    void pause();

    /**
     * 尝试暂停当前任务(不会将当前任务置为空)。
     * 如果指定的id为空，则逻辑同stop()；
     * 如果当前任务是指定的id，则暂停当前任务；否则什么都不做
     * @param taskId 任务的唯一Id
     * @return 如果暂停成功，则返回true；否则返回false
     */
    boolean pause(String taskId);

    /**
     * 尝试暂停当前任务(不会将当前任务置为空)。
     * 如果当前任务的类型是指定的类型，则暂停当前任务；否则什么都不做
     * @param taskType 指定的任务类型
     * @return 如果暂停成功，则返回true；否则返回false
     */
    boolean pauseByType(int taskType);

    /**
     * 尝试暂停当前任务(并将当前任务置为空)。
     * 如果当前没有任务在运行，则什么都不做
     */
    void stop();

    /**
     * 尝试暂停当前任务(并将当前任务置为空)。
     * 如果指定的id为空，则逻辑同stop()；
     * 如果当前任务是指定的id，则暂停当前任务；否则什么都不做
     * @param taskId 任务的唯一Id
     */
    void stop(String taskId);

    /**
     * 尝试暂停当前任务(并将当前任务置为空)。
     * 如果当前任务的类型是指定的类型，则暂停当前任务；否则什么都不做
     * @param taskType 指定的任务类型
     */
    void stopByType(int taskType);

    /**
     * 停止并清空任务队列。
     */
    void stopAndReset();

    /**
     * 设置任务排序器，控制队列中任务的执行顺序。
     * 如果不设置TaskScheduler，则默认按照添加任务顺序执行。
     * @param scheduler 任务排序器
     */
    void setTaskScheduler(TaskScheduler<B> scheduler);

    /**
     * 回调函数。任务完成后调用执行下一个或停止。
     * 注意：task在正常结束时回调此函数，
     * 因为调用任务的pause()或abort()导致中断的，不应该调用此函数
     * @param task 已结束的task
     */
    void notifyTaskFinished(T task);

    /**
     * 注册监听。（不重复注册，根据SerialMgrListener的id判断）
     * @param listener 外部监听者
     */
    void registerListener(SerialMgrListener<B> listener);

    /**
     * 取消注册监听。
     * @param id 外部监听者的唯一id
     */
    void unregisterListener(String id);

    /**
     * 获取所有的外部监听。
     * @return 返回所有的外部监听
     */
    List<SerialMgrListener<B>> getListeners();
}
