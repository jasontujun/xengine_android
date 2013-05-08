package com.xengine.android.full.system.ui;

import com.xengine.android.full.media.audio.XAudio;
import com.xengine.android.full.media.graphics.XGraphics;
import com.xengine.android.full.media.graphics.XScreen;

/**
 * 获取并使用系统的声音、图像、屏幕、http通信等服务对象。
 * Created by 赵之韵.
 * Date: 12-2-29
 * Time: 上午12:44
 */
public interface XServiceManager {
    /**
     * 返回Audio服务
     */
    XAudio audio();

    /**
     * 返回graphics服务
     */
    XGraphics graphics();

    /**
     * 返回screen服务
     */
    XScreen screen();

}
