package com.xengine.android.full.system.ui;

import android.os.Bundle;
import android.os.Handler;

/**
 * Created by 赵之韵.
 * Date: 12-3-1
 * Time: 上午7:18
 */
public interface XUIHandler {
    /**
     * 获取操作窗口的Handler
     */
    Handler getFrameHandler();

    /**
     * 获取操作图层的Handler
     */
    Handler getLayerHandler();

    /**
     * 向图层发消息
     * @param msgWhat 消息类型
     * @param data 消息附带的数据，可以为null
     */
    void sendLayerMessage(int msgWhat, Bundle data);

    /**
     * 向图层发消息
     * @param msgWhat 消息类型
     * @param data 消息附带的数据，可以为null
     */
    void sendFrameMessage(int msgWhat, Bundle data);
}
