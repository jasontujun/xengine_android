package com.xengine.android.base.taskmgr.serial;

import com.xengine.android.base.speed.XBaseSpeedMonitor;
import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.taskmgr.XBaseMgrTaskExecutor;
import com.xengine.android.base.taskmgr.XTaskMgrListener;

import java.util.LinkedList;
import java.util.List;

/**
 * <pre>
 * 用于监测当前SerialTask的速度。
 * User: jasontujun
 * Date: 13-12-21
 * Time: 下午5:03
 * </pre>
 */
public class XSerialSpeedMonitor<B extends XTaskBean>
        extends XBaseSpeedMonitor<XBaseMgrTaskExecutor<B>> {

    private XSerialMgr<XBaseMgrTaskExecutor<B>, B> mSerialMgr;
    private List<XBaseMgrTaskExecutor<B>> mRunningTasks;

    public XSerialSpeedMonitor(XSerialMgr<XBaseMgrTaskExecutor<B>, B> serialMgr) {
        super();
        mSerialMgr = serialMgr;
        mRunningTasks = new LinkedList<XBaseMgrTaskExecutor<B>>();
    }

    public XSerialSpeedMonitor(XSerialMgr<XBaseMgrTaskExecutor<B>, B> serialMgr, int interval) {
        super(interval);
        mSerialMgr = serialMgr;
        mRunningTasks = new LinkedList<XBaseMgrTaskExecutor<B>>();
    }

    @Override
    public List<XBaseMgrTaskExecutor<B>> getRunningTasks() {
        mRunningTasks.clear();
        XBaseMgrTaskExecutor<B> task = mSerialMgr.getRunningTask();
        if (task != null)
            mRunningTasks.add(task);
        return mRunningTasks;
    }

    @Override
    public void notifyUpdateSpeed(XBaseMgrTaskExecutor<B> task, long speed) {
        List<XTaskMgrListener<B>> listeners =  task.getTaskMgr().getListeners();
        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).onSpeedUpdate(task.getBean(), speed);
    }
}
