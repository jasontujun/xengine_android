package com.xengine.android.full.data.cache;

/**
 * 数据仓库。
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-24
 * Time: 上午9:56
 */
public interface XDataRepository {
    void registerDataSource(XDataSource source);
    void unregisterDataSource(XDataSource source);
    XDataSource getSource(String sourceName);
}
