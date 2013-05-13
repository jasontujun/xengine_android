package com.xengine.android.data.cache;

import java.util.Comparator;
import java.util.List;

/**
 * 该接口表明数据源可以被绑定到界面的Adapter上。
 * 其实就是实现了List的一部分子接口。
 * Created by 赵之韵.
 * Date: 11-12-17
 * Time: 上午12:18
 */
public interface XAdapterDataSource<T> extends XDataSource {

    /**
     * 获取数据源中的数据项。
     * @param index 数据项的索引
     */
    T get(int index);

    /**
     * 返回数据源中数据项的数量。
     */
    int size();

    /**
     * 添加数据项
     */
    void add(T item);

    /**
     * 将列表中所有的数据项都添加到数据源当中来
     * @param items 要添加的数据项列表
     */
    void addAll(List<T> items);

    /**
     * 数据源是否是空的。
     */
    boolean isEmpty();

    /**
     * 删除指定的数据项
     * @param index 数据项在数据源中的索引号
     */
    void delete(int index);

    /**
     * 将指定的数据项删除
     */
    void delete(T item);

    /**
     * 将列表中的数据项从数据源中全部删除
     * @param items 要删除的数据项列表
     */
    void deleteAll(List<T> items);

    /**
     * 返回数据项在数据源中的位置
     */
    int indexOf(T item);

    /**
     * 清空数据源中所有的数据项目
     */
    void clear();

    /**
     * 排序
     */
    void sort(Comparator<T> comparator);

    /**
     * 是否包含指定的项目
     */
    boolean contains(T item);

    /**
     * 复制数据源中所有的数据项
     */
    List<T> copyAll();

    /**
     * 通知数据发生了变化
     */
    void notifyDataChanged();

    /**
     * 向数据源注册数据变化的监听器。
     * @param listener 数据变化监听器
     */
    void registerDataChangeListener(XDataChangeListener<T> listener);

    /**
     * 从数据源中注销数据变化的监听器。
     * @param listener 数据变化监听器
     */
    void unregisterDataChangeListener(XDataChangeListener<T> listener);

    /**
     * 设置是否在增删操作时，自动通知监听者
     * @param isAuto
     */
    void setAutoNotifyListeners(boolean isAuto);
}
