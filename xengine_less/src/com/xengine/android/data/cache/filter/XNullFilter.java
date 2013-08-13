package com.xengine.android.data.cache.filter;

import java.util.List;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-18
 * Time: 上午12:18
 */
public class XNullFilter<T> implements XFilter<T> {
    @Override
    public List<T> doFilter(List<T> source) {
        return source;
    }

    @Override
    public T doFilter(T source) {
        return source;
    }
}
