package com.xengine.android.system.ui;

/**
 * UI事件监听器。
 * Created by 赵之韵.
 * Date: 11-12-25
 * Time: 下午3:48
 */
public interface XUIFrameStateListener {

    /**
     * 界面创建完成
     */
    void onFrameCreated();

    /**
     * 界面被显示出来了
     */
    void onFrameDisplay();

    /**
     * 界面将要隐藏
     */
    void onFrameInvisible();

    /**
     * 界面将要退出
     */
    void onFrameExit();
}
