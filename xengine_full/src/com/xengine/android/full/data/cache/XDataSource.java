package com.xengine.android.full.data.cache;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-3
 * Time: 下午1:15
 */
public interface XDataSource {
    /**
     * 返回数据源的名称，数据源在数据仓库中以数据源名称为唯一标示。
     */
    String getSourceName();
}
