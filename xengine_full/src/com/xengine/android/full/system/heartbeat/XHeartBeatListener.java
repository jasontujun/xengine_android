package com.xengine.android.full.system.heartbeat;

/**
 * 心跳事件监听器
 * Created by 赵之韵.
 * Date: 11-12-6
 * Time: 下午9:42
 */
public interface XHeartBeatListener {
    /**
     * 发生系统心跳事件
     * @param when 心跳的时间
     */
    void onHeartBeatEvent(long when);
}
