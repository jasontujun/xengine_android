package com.xengine.android.base.taskmgr.parallel;

import com.xengine.android.base.speed.XBaseSpeedMonitor;
import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.taskmgr.XBaseMgrTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgrListener;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 用于监测当前SerialTask的速度。
 * User: jasontujun
 * Date: 13-12-21
 * Time: 下午5:03
 * </pre>
 */
public class XParallelSpeedMonitor<B extends XTaskBean>
        extends XBaseSpeedMonitor<XBaseMgrTaskExecutor<B>> {

    private XParallelMgr<XBaseMgrTaskExecutor<B>, B> mParallelMgr;
    private List<XBaseMgrTaskExecutor<B>> mRunningTasks;

    public XParallelSpeedMonitor(XParallelMgr<XBaseMgrTaskExecutor<B>, B> parallelMgr) {
        super();
        mParallelMgr = parallelMgr;
        mRunningTasks = new ArrayList<XBaseMgrTaskExecutor<B>>();
    }

    public XParallelSpeedMonitor(XParallelMgr<XBaseMgrTaskExecutor<B>, B> parallelMgr, int interval) {
        super(interval);
        mParallelMgr = parallelMgr;
        mRunningTasks = new ArrayList<XBaseMgrTaskExecutor<B>>();
    }

    @Override
    public List<XBaseMgrTaskExecutor<B>> getRunningTasks() {
        mRunningTasks.clear();
        mRunningTasks.addAll(mParallelMgr.getRunningTask());
        return mRunningTasks;
    }

    @Override
    public void notifyUpdateSpeed(XBaseMgrTaskExecutor<B> task, long speed) {
        List<XTaskMgrListener<B>> listeners =  task.getTaskMgr().getListeners();
        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).onSpeedUpdate(task.getBean(), speed);
    }
}
