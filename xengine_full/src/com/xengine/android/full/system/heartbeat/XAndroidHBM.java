package com.xengine.android.full.system.heartbeat;

import com.xengine.android.full.system.ssm.XSystemState;
import com.xengine.android.full.system.ssm.XSystemStateListener;
import com.xengine.android.full.utils.XLog;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 心跳管理模块。
 * Created by 赵之韵.
 * Date: 11-12-6
 * Time: 下午9:43
 */
public class XAndroidHBM implements XHeartBeatManager, XSystemStateListener {
    private static final String TAG = XAndroidHBM.class.getCanonicalName();

    private static XAndroidHBM instance;

    public synchronized static XAndroidHBM getInstance() {
        if(instance == null) {
            instance = new XAndroidHBM();
        }
        return instance;
    }

    private XAndroidHBM() {
        timer = new Timer();
    }

    /**
     * 心跳事件的观察者
     */
    private ArrayList<XHeartBeatListener> listeners = new ArrayList<XHeartBeatListener>();

    /**
     * 计时器
     */
    private Timer timer;

    /**
     * 每一次心跳之间的间隔时间（默认30秒跳一次）
     */
    private int beatInterval = 10 * 1000;

    /**
     * 心跳用到的timertask
     */
    private BeatTask beat;

    /**
     * 每一次心跳执行的任务
     */
    private class BeatTask extends TimerTask {
        @Override
        public void run() {
            long time = System.currentTimeMillis();
            XLog.d(TAG, "Heartbeat event: " + time);
            for(int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onHeartBeatEvent(time);
            }
        }
    }

    @Override
    public void setHeartBeatInterval(int interval) {
        beatInterval = interval;
    }

    @Override
    public int getHeartBeatInterval() {
        return beatInterval;
    }

    @Override
    public void startHeartBeat() {
        XLog.d(TAG, "Start heartbeat");
        beat = new BeatTask();
        timer.scheduleAtFixedRate(beat, 0, beatInterval);
    }

    @Override
    public void stopHeartBeat() {
        XLog.d(TAG, "Stop heartbeat");
        beat.cancel();
    }

    @Override
    public void registerHeartBeatListener(XHeartBeatListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterHeartBeatListener(XHeartBeatListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onSystemStateChanged(XSystemState newState) {
        switch (newState) {
            case ACTIVE:
                startHeartBeat();
                break;
            case INACTIVE:
                stopHeartBeat();
                break;
        }
    }
}