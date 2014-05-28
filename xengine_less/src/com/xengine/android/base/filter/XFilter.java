package com.xengine.android.base.filter;

import java.util.List;

/**
 * 过滤器接口。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-17
 * Time: 下午11:55
 */
public interface XFilter<T> {
    /**
     * 返回源数据列表中符合过滤条件的项目。
     * （注意不要对输入的参数source进行修改稿）
     */
    List<T> doFilter(List<T> source);

    /**
     * 如果满足条件则返回源数据，如果不满足条件则返回null
     */
    T doFilter(T source);
}
