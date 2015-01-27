package com.xengine.android.toolkit.listener;

import java.util.List;

/**
 * <pre>
 * 监听管理器接口
 * User: jasontujun
 * Date: 14-5-28
 * Time: 上午9:52
 * </pre>
 */
public interface XListenerMgr<T> {

    /**
     * 注册监听
     * @param listener 监听
     * @return 注册成功返回true;否则返回false
     */
    boolean registerListener(T listener);

    /**
     * 解除注册监听
     * @param listener 监听
     * @return 注册成功返回true;否则返回false
     */
    boolean unregisterListener(T listener);

    /**
     * 获取所有监听
     * @return 返回监听列表
     */
    List<T> getListeners();
}
