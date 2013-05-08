package com.xengine.android.data.cache;

import java.util.Comparator;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-30
 * Time: 下午2:30
 */
public interface XWithFilter<T> {
    void registerDataChangeListenerForOrigin(XDataChangeListener<T> listener);
    void unregisterDataChangeListenerForOrigin(XDataChangeListener<T> listener);
    void setFilter(XFilter<T> filter);
    XFilter<T> getFilter();
    void doFilter();
    T getOrigin(int i);
    int sizeOrigin();
    void sortOrigin(Comparator<T> comparator);
}
