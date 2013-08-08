package com.xengine.android.media.image.loader;

/**
 * 滑动时延迟加载的特性。用于图片加载器
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 下午4:36
 * To change this template use File | Settings | File Templates.
 */
public interface XScrollLazyLoading {
    void wakeUp();
    void fallAsleep();
    void fallAsleepAndClear();
}
