package com.xengine.android.full.data.cache;

import java.util.List;

/**
 * 数据变化监听器接口，监听数据源中的数据变化。
 */
public interface XDataChangeListener<T> {

    /**
     * 总体的改变
     */
    void onChange();

    /**
     * 添加了数据
     */
    void onAdd(T item);

    /**
     * 添加了一个列表的数据
     */
    void onAddAll(List<T> items);

    /**
     * 删除了数据项
     */
    void onDelete(T item);

    /**
     * 删除了一个列表的数据项
     */
    void onDeleteAll(List<T> items);

}
