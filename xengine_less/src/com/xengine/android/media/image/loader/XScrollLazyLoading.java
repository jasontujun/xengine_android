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
    /**
     * 停止滑动，开始加载。
     */
    void onIdle();

    /**
     * 滑动中，停止加载。
     */
    void onScroll();

    /**
     * 停止加载并清空加载任务。
     */
    void stopAndClear();
}
