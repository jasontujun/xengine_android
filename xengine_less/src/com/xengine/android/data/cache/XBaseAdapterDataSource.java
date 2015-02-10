package com.xengine.android.data.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 实现XAdapterDataSource接口的数据源抽象类。
 * Created by 赵之韵.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterDataSource<T> implements XAdapterDataSource<T> {

    /**
     * 实际的对象列表。
     */
    protected ArrayList<T> mItemList;

    /**
     * 数据变化监听器
     */
    protected List<XDataChangeListener<T>> mListeners;

    /**
     * 自动通知监听者
     */
    protected boolean mIsAutoNotify;

    public XBaseAdapterDataSource() {
        mItemList = new ArrayList<T>();
        mListeners = new CopyOnWriteArrayList<XDataChangeListener<T>>();
        mIsAutoNotify = true;
    }

    @Override
    public void sort(Comparator<T> comparator) {
        Collections.sort(mItemList, comparator);
    }

    @Override
    public T get(int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return mItemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if (!mItemList.contains(item)) {
            mItemList.add(item);
            if (mIsAutoNotify)
                notifyAddItem(item);
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if (items == null)
            return;

        for (T item: items) {
            if(!mItemList.contains(item))
                mItemList.add(item);
        }
        if (mIsAutoNotify)
            notifyAddItems(items);
    }

    @Override
    public boolean isEmpty() {
        return mItemList.isEmpty();
    }

    @Override
    public synchronized void delete(int index) {
        if (index < 0 || index >= mItemList.size())
            return;

        T item = mItemList.remove(index);
        if (mIsAutoNotify)
            notifyDeleteItem(item);
    }

    @Override
    public synchronized void delete(T item) {
        if (mItemList.remove(item)) {
            if (mIsAutoNotify)
                notifyDeleteItem(item);
        }
    }

    @Override
    public synchronized void deleteAll(List<T> items) {
        if (mItemList.removeAll(items)) {
            if (mIsAutoNotify)
                notifyDeleteItems(items);
        }
    }

    @Override
    public int indexOf(T item) {
        return mItemList.indexOf(item);
    }

    @Override
    public boolean contains(T item) {
        return mItemList.contains(item);
    }

    /**
     * 返回数据源中所有的数据项的副本
     */
    @Override
    public List<T> copyAll() {
        return new ArrayList<T>(mItemList);
    }

    @Override
    public synchronized void clear() {
        List<T> copyItems = new ArrayList<T>(mItemList);
        mItemList.clear();
        if (mIsAutoNotify)
            notifyDeleteItems(copyItems);
    }

    @Override
    public void registerDataChangeListener(XDataChangeListener<T> listener) {
        if (!mListeners.contains(listener))
            mListeners.add(listener);
    }

    @Override
    public void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        mListeners.remove(listener);
    }

    @Override
    public void notifyDataChanged() {
        for (XDataChangeListener<T> listener: mListeners) {
            listener.onChange();
        }
    }

    protected void notifyAddItem(T item) {
        for (XDataChangeListener<T> listener: mListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        for (XDataChangeListener<T> listener: mListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        for (XDataChangeListener<T> listener: mListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        for (XDataChangeListener<T> listener: mListeners)
            listener.onDeleteAll(items);
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.mIsAutoNotify = isAuto;
    }
}
