package com.xengine.android.data.cache;

import com.xengine.android.base.listener.XCowListenerMgr;
import com.xengine.android.base.listener.XListenerMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 基于Id唯一标识每个数据的数据源抽象类。
 * Created by jasontujun.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterIdDataSource<T> implements XAdapterDataSource<T>, XWithId<T> {

    /**
     * 实际的对象列表。
     */
    protected ArrayList<T> mItemList;

    /**
     * 数据变化监听器
     */
    protected XListenerMgr<XDataChangeListener<T>> mListeners;

    /**
     * 自动通知监听者
     */
    protected boolean mIsAutoNotify;

    protected XBaseAdapterIdDataSource() {
        mItemList = new ArrayList<T>();
        mListeners = new XCowListenerMgr<XDataChangeListener<T>>();
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
    public T getById(String id) {
        int index = getIndexById(id);
        if (index != -1)
            return get(index);
        else
            return null;
    }

    @Override
    public int size() {
        return mItemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if (item == null)
            return;

        int index = getIndexById(getId(item));
        if (index == -1) {
            mItemList.add(item);
            if (mIsAutoNotify)
                notifyAddItem(item);
        } else {
            replace(index, item);
            if (mIsAutoNotify)
                notifyDataChanged();
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if (items == null || items.size() == 0)
            return;

        for (int i = 0; i<items.size(); i++) {
            T item = items.get(i);
            int index = getIndexById(getId(item));
            if (index == -1) {
                mItemList.add(item);
            } else {
                replace(index, item);
            }
        }
        if (mIsAutoNotify)
            notifyAddItems(items);
    }

    @Override
    public boolean isEmpty() {
        return mItemList.isEmpty();
    }

    @Override
    public synchronized void deleteById(String id) {
        int index = getIndexById(id);
        if (index != -1)
            delete(index);
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
    public synchronized void deleteAllById(List<String> ids) {
        if (ids == null || ids.size() == 0)
            return;
        List<T> items = new ArrayList<T>();
        for (int i = 0; i < ids.size(); i++) {
            T item = getById(ids.get(i));
            if (item != null) {
                items.add(item);
            }
        }
        deleteAll(items);
    }

    @Override
    public int indexOf(T item) {
        return mItemList.indexOf(item);
    }

    @Override
    public boolean contains(T item) {
        if (item == null)
            return false;

        final String id = getId(item);
        for (int i = 0; i<size(); i++) {
            T tmp = get(i);
            if (getId(tmp).equals(id)) {
                return true;
            }
        }
        return false;
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
        mListeners.registerListener(listener);
    }

    @Override
    public synchronized void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        mListeners.unregisterListener(listener);
    }

    @Override
    public void notifyDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners) {
            listener.onChange();
        }
    }

    protected void notifyAddItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    /**
     * 替换某一项（用于重复添加的情况）
     * TIP 子类可覆盖此方法
     * @param index
     * @param newItem
     */
    @Override
    public void replace(int index, T newItem) {
        mItemList.set(index, newItem);
    }

    @Override
    public int getIndexById(String id) {
        for (int i = 0; i<size(); i++) {
            T tmp = get(i);
            if (getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.mIsAutoNotify = isAuto;
    }
}
