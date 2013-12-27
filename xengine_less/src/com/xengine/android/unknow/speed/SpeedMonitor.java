package com.xengine.android.unknow.speed;

import tv.pps.module.download.core.speed.calc.SpeedCalculator;

import java.util.List;

/**
 * <pre>
 * 监测当前任务网速的监视线程。
 * T 表示监测测任务类型。
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-10-22
 * Time: 下午4:07
 * </pre>
 */
public interface SpeedMonitor<T> {

    void start();

    void stop();

    List<T> getRunningTasks();

    SpeedCalculator getSpeedCalculator(T task);

    /**
     * 获取任务当前已完成的大小。用于计算速度。
     * @param task
     * @return
     */
    long getCompleteSize(T task);

    /**
     * 回调函数：通知对应任务更新当前速度。
     * @param task 当前任务
     * @param speed 当前速度（单位：byte/s）
     */
    void notifyUpdateSpeed(T task, long speed);
}
