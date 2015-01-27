package com.xengine.android.toolkit.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * CopyOnWrite方式实现的监听管理器
 * User: jasontujun
 * Date: 14-5-28
 * Time: 上午10:00
 * </pre>
 */
public class XCowListenerMgr<T> implements XListenerMgr<T> {

    private List<T> mListeners;

    public XCowListenerMgr() {
        mListeners = new ArrayList<T>();
    }

    @Override
    public synchronized boolean registerListener(T listener) {
        if (listener == null)
            return false;

        if (mListeners.contains(listener))
            return false;

        List<T> copyListeners = new ArrayList<T>(mListeners);
        copyListeners.add(listener);
        mListeners = copyListeners;
        return true;
    }

    @Override
    public synchronized boolean unregisterListener(T listener) {
        if (listener == null)
            return false;

        List<T> copyListeners = new ArrayList<T>(mListeners);
        if (!copyListeners.remove(listener))
            return false;

        mListeners = copyListeners;
        return true;
    }

    @Override
    public List<T> getListeners() {
        return mListeners;
    }
}
