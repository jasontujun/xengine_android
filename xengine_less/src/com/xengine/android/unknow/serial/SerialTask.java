package com.xengine.android.unknow.serial;

import tv.pps.module.download.core.speed.calc.SpeedCalculator;
import tv.pps.module.download.core.task.TaskBean;
import tv.pps.module.download.core.task.TaskExecutor;

/**
 * <pre>
 * User: jasontujun
 * Date: 13-12-21
 * Time: 下午4:49
 * </pre>
 */
public interface SerialTask extends TaskExecutor {

    /**
     * 添加线性管理器作为监听者。
     * 当任务结束时，调用SerialMgr的notifyTaskFinished()
     * @param serialMgr
     */
    void setSerialMgr(SerialMgr<SerialTask, TaskBean> serialMgr);

    /**
     * 获取任务所属的线性管理器。
     * @return
     */
    SerialMgr<SerialTask, TaskBean> getSerialMgr();

    /**
     * 设置任务的速度计算器
     * @param speedCalculator
     */
    void setSpeedCalculator(SpeedCalculator speedCalculator);

    /**
     * 获取任务自己的速度计算器
     * @return
     */
    SpeedCalculator getSpeedCalculator();
}
