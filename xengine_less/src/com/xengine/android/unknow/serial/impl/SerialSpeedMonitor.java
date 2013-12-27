package com.xengine.android.unknow.serial.impl;

import tv.pps.module.download.core.serial.SerialMgrListener;
import tv.pps.module.download.core.serial.SerialTask;
import tv.pps.module.download.core.speed.BaseSpeedMonitor;
import tv.pps.module.download.core.speed.calc.SpeedCalculator;
import tv.pps.module.download.core.task.TaskBean;

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
public class SerialSpeedMonitor extends BaseSpeedMonitor<SerialTask> {

    private SerialMgrImpl mDownloadMgr;
    private List<SerialTask> mRunningTasks;

    public SerialSpeedMonitor(SerialMgrImpl downloadMgr) {
        super(DEFAULT_INTERVAL);
        mDownloadMgr = downloadMgr;
        mRunningTasks = new LinkedList<SerialTask>();
    }

    @Override
    public List<SerialTask> getRunningTasks() {
        mRunningTasks.clear();
        SerialTask task = mDownloadMgr.getRunningTask();
        if (task != null)
            mRunningTasks.add(task);
        return mRunningTasks;
    }

    @Override
    public SpeedCalculator getSpeedCalculator(SerialTask task) {
        return task.getSpeedCalculator();
    }

    @Override
    public long getCompleteSize(SerialTask task) {
        return task.getBean().getCompleteSize();
    }

    @Override
    public void notifyUpdateSpeed(SerialTask task, long speed) {
        List<SerialMgrListener<TaskBean>> listeners =  task.getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onSpeedUpdate(task.getBean(), speed);
    }
}
