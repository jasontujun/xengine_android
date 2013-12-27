package com.xengine.android.unknow.speed;

import tv.pps.module.download.core.speed.calc.SpeedCalculator;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <pre>
 * 实现SpeedMonitor接口的抽象类，封装了大部分速度监测逻辑。
 * 继承者只需要实现getCompleteSize(),notifyUpdateSpeed()等抽象方法即可。
 * User: jasontujun
 * Date: 13-10-22
 * Time: 下午5:03
 * </pre>
 */
public abstract class BaseSpeedMonitor<T> implements SpeedMonitor<T> {
    public static final int DEFAULT_INTERVAL = 1000;// 默认刷新间隔，1秒

    private boolean mRunning;// 标识监测线程是否在运行
    private Timer mMonitorThread;// 监视线程
    protected int mInterval;// 刷新间隔

    public BaseSpeedMonitor(int interval) {
        mInterval = interval;
        if (interval <= 0)
            mInterval = DEFAULT_INTERVAL;
    }

    @Override
    public void start() {
        if (mRunning)
            return;
        mRunning = true;

        mMonitorThread = new Timer();
        mMonitorThread.scheduleAtFixedRate(new MonitorSpeed(), 0, mInterval);
    }

    @Override
    public void stop() {
        if (!mRunning)
            return;
        mRunning = false;

        mMonitorThread.cancel();
    }

    private class MonitorSpeed extends TimerTask {
        @Override
        public void run() {
            if (!mRunning)
                return;

            List<T> tasks = getRunningTasks();
            if (tasks == null)
                return;
            for (T task : tasks) {
                if (!mRunning)
                    return;

                SpeedCalculator calculator = getSpeedCalculator(task);
                if (calculator != null) {
                    long speed = calculator.getSpeed(getCompleteSize(task));// 计算下载速度
                    notifyUpdateSpeed(task, speed);
                }
            }
        }
    }
}
