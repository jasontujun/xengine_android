package com.xengine.android.system.ssm;

/**
 * 系统状态监听器。
 * 监听整个系统的创建、恢复、暂停、销毁状态。
 * Created by 赵之韵.
 * Date: 11-12-8
 * Time: 上午10:40
 * @see com.morln.app.system.ssm.XSystemState
 */
public interface XSystemStateListener {

    /**
     * 系统状态发生改变
     * @param newState 系统的新状态
     */
    void onSystemStateChanged(XSystemState newState);
}
