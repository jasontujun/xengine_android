package com.xengine.android.base.taskmgr.parallel;

import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.task.XTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgr;

import java.util.List;

/**
 * <pre>
 * 并行任务执行器接口。
 * T表示任务的类型
 * B表示数据的类型
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 * </pre>
 */
public interface XParallelMgr<T extends XTaskExecutor<B>, B extends XTaskBean>
        extends XTaskMgr<T, B> {

    /**
     * 运行队列是否已空
     * @return 已空则返回true；否则返回false
     */
    boolean isEmptyParallel();

    /**
     * 运行队列是否已满
     * @return 已满则返回true；否则返回false
     */
    boolean isFullParallel();

    /**
     * 是否所有任务都停止
     * @return
     */
    boolean isAllStop();

    /**
     * 如果运行队列未满，则将指定Id的任务
     * 从等待队列添加进运行队列，且不启动执行。
     * 如果运行队列已满，则什么都不做。
     * @param taskId 任务的唯一Id
     */
    void setRunningTask(String taskId);

    /**
     * 获取运行队列的所有任务
     * @return 返回当前正在运行的任务，如果没有，则返回null
     */
    List<T> getRunningTask();

    /**
     * 获取当前等待队列的所有任务
     * @return 返回等待执行的任务列表
     */
    List<T> getWaitingTask();
}
