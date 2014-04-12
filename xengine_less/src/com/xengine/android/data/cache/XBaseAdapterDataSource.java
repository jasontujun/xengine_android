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
    protected List<XDataChangeListener<T>> listeners = new ArrayList<XDataChangeListener<T>>();

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
                notifyAddItem(item);
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
            notifyAddItems(items);
    }

    @Override
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    @Override
    public synchronized void delete(int index) {
        if (index < 0 || index >= itemList.size())
            return;

        T item = itemList.remove(index);
        if (isAutoNotify)
            notifyDeleteItem(item);
    }

    @Override
    public synchronized void delete(T item) {
        if (itemList.remove(item)) {
            if (isAutoNotify)
                notifyDeleteItem(item);
        }
    }

    @Override
    public synchronized void deleteAll(List<T> items) {
        if (itemList.removeAll(items)) {
            if (isAutoNotify)
                notifyDeleteItems(items);
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
        List<T> copyItems = new ArrayList<T>(itemList);
        itemList.clear();
        if (isAutoNotify)
            notifyDeleteItems(copyItems);
    }

    @Override
    public synchronized void registerDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(listeners);
            if (!copyListeners.contains(listener)) {
                copyListeners.add(listener);
                listeners = copyListeners;
            }
        }
    }

    @Override
    public synchronized void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(listeners);
            if (copyListeners.remove(listener))
                listeners = copyListeners;
        }
    }

    @Override
    public void notifyDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners) {
            listener.onChange();
        }
    }

    protected void notifyAddItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = listeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }
}
