package com.xengine.android.data.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 内存中的数据源，可以用于适配界面Adapter。
 * Created by 赵之韵.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterDataSource<T> implements XAdapterDataSource<T> {

    /**
     * 实际的对象列表。
     */
    protected ArrayList<T> itemList = new ArrayList<T>();

    /**
     * 数据变化监听器
     */
    protected ArrayList<XDataChangeListener<T>> listeners = new ArrayList<XDataChangeListener<T>>();

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify = true;

    @Override
    public void sort(Comparator<T> comparator) {
        Collections.sort(itemList, comparator);
    }

    @Override
    public T get(int index) {
        return itemList.get(index);
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if (!itemList.contains(item)) {
            itemList.add(item);
            if (isAutoNotify)
                for (XDataChangeListener<T> listener: listeners) {
                    listener.onAdd(item);
                }
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if (items == null)
            return;

        for (T item: items) {
            if(!itemList.contains(item))
                itemList.add(item);
        }
        if (isAutoNotify)
            for(XDataChangeListener<T> listener: listeners) {
                listener.onAddAll(items);
            }
    }

    @Override
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    @Override
    public synchronized void delete(int index) {
        T item = itemList.remove(index);
        if (isAutoNotify)
            for (XDataChangeListener<T> listener: listeners) {
                listener.onDelete(item);
            }
    }

    @Override
    public synchronized void delete(T item) {
        if (itemList.remove(item)) {
            if (isAutoNotify)
                for (XDataChangeListener<T> listener: listeners) {
                    listener.onDelete(item);
                }
        }
    }

    @Override
    public synchronized void deleteAll(List<T> items) {
        if (itemList.removeAll(items)) {
            if (isAutoNotify)
                for (XDataChangeListener<T> listener: listeners) {
                    listener.onDeleteAll(items);
                }
        }
    }

    @Override
    public int indexOf(T item) {
        return itemList.indexOf(item);
    }

    @Override
    public boolean contains(T item) {
        return itemList.contains(item);
    }

    /**
     * 返回数据源中所有的数据项的副本
     */
    @Override
    public List<T> copyAll() {
        return new ArrayList<T>(itemList);
    }

    @Override
    public synchronized void clear() {
        if (isAutoNotify)
            for (XDataChangeListener<T> listener: listeners) {
                listener.onDeleteAll(itemList);
            }
        itemList.clear();
    }

    @Override
    public void registerDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyDataChanged() {
        for (XDataChangeListener<T> listener: listeners) {
            listener.onChange();
        }
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }
}
