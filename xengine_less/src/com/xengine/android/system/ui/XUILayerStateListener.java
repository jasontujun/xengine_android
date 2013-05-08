package com.xengine.android.system.ui;

/**
 * Created by 赵之韵.
 * Date: 12-1-11
 * Time: 上午1:06
 */
public interface XUILayerStateListener {
    /**
     * 图层被添加到窗口中
     */
    void onLayerAddedToFrame();

    /**
     * 图层被新的图层覆盖了
     */
    void onLayerCovered();

    /**
     * 覆盖本图层的图层退出了，本图层重新显示出来
     */
    void onLayerUnCovered();

    /**
     * 图层从窗口中删除
     */
    void onLayerRemovedFromFrame();
}
