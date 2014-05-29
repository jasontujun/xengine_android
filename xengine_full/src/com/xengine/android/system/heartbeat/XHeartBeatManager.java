package com.xengine.android.system.heartbeat;

/**
 * 心跳管理器。
 * 在系统中向所有的监听者广播心跳事件。
 * Created by 赵之韵.
 * Date: 11-12-18
 * Time: 下午5:05
 */
public interface XHeartBeatManager {

    /**
     * 设置心跳事件之间的间隔时间。
     */
    void setHeartBeatInterval(int interval);

    /**
     * 返回心跳事件之间的间隔时间。
     * @return
     */
    int getHeartBeatInterval();

    /**
     * 开始心跳
     */
    void startHeartBeat();

    /**
     * 停止心跳
     */
    void stopHeartBeat();

    /**
     * 注册心跳事件监听器
     */
    void registerHeartBeatListener(XHeartBeatListener listener);

    /**
     * 注销心跳事件监听器
     */
    void unregisterHeartBeatListener(XHeartBeatListener listener);
}
