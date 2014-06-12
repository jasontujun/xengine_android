package com.xengine.android.base.taskmgr.serial;

import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.taskmgr.XMgrTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgr;

import java.util.List;

/**
 * <pre>
 * 线性任务执行器接口。
 * T表示任务的类型
 * B表示数据的类型
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午8:48
 * </pre>
 */
public interface XSerialMgr<B extends XTaskBean>
        extends XTaskMgr<XMgrTaskExecutor<B>, B> {

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
    XMgrTaskExecutor<B> getRunningTask();

    /**
     * 获取当前等待队列的所有任务
     * @return 返回等待执行的任务列表
     */
    List<XMgrTaskExecutor<B>> getWaitingTask();
}
