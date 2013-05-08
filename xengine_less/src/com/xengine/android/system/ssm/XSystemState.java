package com.xengine.android.system.ssm;

/**
 * 整个系统的状态。
 * 是ActivityState的一个子集。
 * Created by 赵之韵.
 * Date: 11-12-18
 * Time: 上午9:21
 */
public enum XSystemState {
    /**
     * 系统刚刚创建，通常是第一个activity调用onCreate()的时候。
     */
    ESTABLISHED,
    /**
     * 系统进入活跃状态
     */
    ACTIVE,
    /**
     * 系统退出活跃状态，通常是最后一个activity调用onPause()的时候。
     */
    INACTIVE,
    /**
     * 系统完全退出
     */
    EXIT;
}
