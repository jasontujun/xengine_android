package com.xengine.android.full.system.ssm;

import com.xengine.android.full.system.ui.XUIFrame;
import com.xengine.android.full.system.ui.XUIFrameState;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-1
 * Time: 上午11:31
 */
public interface XSystemStateManager {
    /**
     * 注册系统状态监听器
     */
    void registerSystemStateListener(XSystemStateListener listener);
    /**
     * 注销系统状态监听器
     */
    void unregisterSystemStateListener(XSystemStateListener listener);
    /**
     * 通知系统状态管理器，窗口的状态改变了
     * @param uiFrame 窗口
     * @param newState 新的状态
     */
    void notifyUIStateChanged(XUIFrame uiFrame, XUIFrameState newState);
    /**
     * 返回当前系统的状态。
     */
    XSystemState getCurrentSystemState();
    /**
     * 获取当前处于活跃状态的窗口
     */
    XUIFrame getCurrentActiveUIFrame();
}
