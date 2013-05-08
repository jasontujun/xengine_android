package com.xengine.android.full.system.ui;

/**
 * 窗口的状态，对应Activity的生命周期函数。
 * Created by 赵之韵.
 * Date: 11-12-18
 * Time: 上午2:07
 */
public enum XUIFrameState {
    /**
     * onCreate()
     */
    CREATED,
    /**
     * onStart();
     */
    STARTED,
    /**
     * onRestart();
     */
    RESTARTED,
    /**
     * onResume();
     */
    RESUMED,
    /**
     * onPause();
     */
    PAUSED,
    /**
     * onStop();
     */
    STOPPED,
    /**
     * onDestroy();
     */
    DESTROYED;

    /**
     * 判断状态是否处于可见周期内（从onStart()到onStop()）
     */
    public static boolean inVisibleState(XUIFrameState state) {
        if(state == STARTED || state == RESUMED || state == PAUSED) {
            return true;
        }else {
            return false;
        }
    }
}
