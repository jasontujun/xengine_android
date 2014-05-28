package com.xengine.android.data.cache;

import com.xengine.android.base.filter.XFilter;
import com.xengine.android.base.listener.XCowListenerMgr;
import com.xengine.android.base.listener.XListenerMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 基于Id唯一标识每个数据的带过滤功能的数据源抽象类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-17
 * Time: 下午11:43
 */
public abstract class XBaseFilteredAdapterIdSource<T>
        implements XFilteredAdapterDataSource<T>, XWithId<T> {

    protected XFilter<T> mFilter;
    protected ArrayList<T> mItemList;
    protected ArrayList<T> mCache;
    protected XListenerMgr<XDataChangeListener<T>> mListeners;
    protected XListenerMgr<XDataChangeListener<T>> mOriginListeners;
    protected Comparator<T> mComparator;

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify;

    public XBaseFilteredAdapterIdSource() {
        mItemList = new ArrayList<T>();
        mCache = new ArrayList<T>();
        mListeners = new XCowListenerMgr<XDataChangeListener<T>>();
        mOriginListeners = new XCowListenerMgr<XDataChangeListener<T>>();
        isAutoNotify = true;
        doFilter();
    }

    @Override
    public void sort(Comparator<T> comparator) {
        mComparator = comparator;
        Collections.sort(mCache, comparator);
        if (isAutoNotify)
            notifyCacheDataChanged();
    }

    @Override
    public void sortOrigin(Comparator<T> comparator) {
        mComparator = comparator;
        Collections.sort(mItemList, comparator);
        if (isAutoNotify)
            notifyOriginDataChanged();
    }

    @Override
    public void setFilter(XFilter<T> filter) {
        this.mFilter = filter;
        doFilter();
    }

    @Override
    public XFilter<T> getFilter() {
        return mFilter;
    }

    @Override
    public void doFilter() {
        mCache.clear();
        if (mFilter == null)
            mCache.addAll(mItemList);
        else
            mCache.addAll(mFilter.doFilter(mItemList));
        if (mComparator != null) {
            Collections.sort(mCache, mComparator);
            Collections.sort(mItemList, mComparator);
        }

        if (isAutoNotify)
            notifyDataChanged();
    }

    @Override
    public T get(int i) {
        return mCache.get(i);
    }
    @Override
    public T getOrigin(int i) {
        return mItemList.get(i);
    }

    @Override
    public T getById(String id) {
        int originIndex = getOriginIndexOf(id);
        if (originIndex != -1)
            return mItemList.get(originIndex);
        else
            return null;
    }

    @Override
    public int size() {
        return mCache.size();
    }


    @Override
    public int sizeOrigin() {
        return mItemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if (item == null)
            return;

        int originIndex = getOriginIndexOf(getId(item));
        if (originIndex == -1) {
            mItemList.add(item);
            if (isAutoNotify)
                notifyAddOriginItem(item);

            T result = (mFilter != null) ? mFilter.doFilter(item) : item;
            if (result != null) {
                mCache.add(result);
                if (isAutoNotify)
                    notifyAddItem(item);
            }
        } else {
            replace(originIndex, item);
            if (isAutoNotify)
                notifyOriginDataChanged();

            T result = (mFilter != null) ? mFilter.doFilter(item) : item;
            if (result != null && getIndexById(getId(item)) == -1) {
                mCache.add(result);
                if (isAutoNotify)
                    notifyAddItem(item);
            }
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if (items == null || items.size() == 0)
            return;

        ArrayList<T> addedToOrigin = new ArrayList<T>();
        ArrayList<T> addedToCache = new ArrayList<T>();

        for(T item: items) {
            int originIndex = getOriginIndexOf(getId(item));
            if (originIndex == -1) {
                mItemList.add(item);
                addedToOrigin.add(item);

                T result = (mFilter != null) ? mFilter.doFilter(item) : item;
                if (result != null) {
                    mCache.add(result);
                    addedToCache.add(item);
                }
            } else {
                replace(originIndex, item);

                T result = (mFilter != null) ? mFilter.doFilter(item) : item;
                if (result != null) {
                    int index = getIndexById(getId(item));
                    if (index == -1) {
                        mCache.add(result);
                        addedToCache.add(item);
                    }
                }
            }
        }
        if (isAutoNotify) {
            notifyAddOriginItems(addedToOrigin);
            notifyAddItems(addedToCache);
        }
    }

    @Override
    public boolean isEmpty() {
        return mCache.size() == 0;
    }

    @Override
    public synchronized void deleteById(String id) {
        T item = getById(id);
        if (item != null)
            delete(item);
    }

    @Override
    public synchronized void delete(T item) {
        boolean originDeleted = mItemList.remove(item);
        boolean cacheDeleted = mCache.remove(item);
        if (isAutoNotify) {
            if (originDeleted)
                notifyDeleteOriginItem(item);
            if (cacheDeleted)
                notifyDeleteItem(item);
        }
    }

    @Override
    public synchronized void delete(int index) {
        if (index < 0 || index >= mCache.size())
            return;

        T item = mCache.get(index);
        if (item != null)
            delete(item);
    }

    @Override
    public synchronized void deleteAll(List<T> items) {
        List<T> copyDeleted = new ArrayList<T>(items);
        boolean originDeleted = mItemList.removeAll(copyDeleted);
        boolean cacheDeleted = mCache.removeAll(copyDeleted);
        if (isAutoNotify) {
            if (originDeleted)
                notifyDeleteOriginItems(copyDeleted);
            if (cacheDeleted)
                notifyDeleteItems(copyDeleted);
        }
    }

    @Override
    public synchronized void deleteAllById(List<String> ids) {
        if (ids == null || ids.size() == 0)
            return;
        List<T> items = new ArrayList<T>();
        for (String id : ids) {
            T item = getById(id);
            if (item != null) {
                items.add(item);
            }
        }
        deleteAll(items);
    }

    @Override
    public void notifyDataChanged() {
        notifyOriginDataChanged();
        notifyCacheDataChanged();
    }

    protected void notifyCacheDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onChange();
    }

    protected void notifyOriginDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onChange();
    }

    protected void notifyAddItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddOriginItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyAddOriginItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteOriginItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    protected void notifyDeleteOriginItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners.getListeners();
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    @Override
    public int indexOf(T item) {
        return mCache.indexOf(item);
    }

    @Override
    public void clear() {
        List<T> copyCache = new ArrayList<T>(mCache);
        List<T> copyItems = new ArrayList<T>(mItemList);
        mCache.clear();
        mItemList.clear();
        if (isAutoNotify) {
            notifyDeleteOriginItems(copyItems);
            notifyDeleteItems(copyCache);
        }
    }

    @Override
    public boolean contains(T item) {
        return mCache.contains(item);
    }

    @Override
    public List<T> copyAll() {
        return new ArrayList<T>(mCache);
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
    public synchronized void registerDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        mOriginListeners.registerListener(listener);
    }

    @Override
    public synchronized void unregisterDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        mOriginListeners.unregisterListener(listener);
    }

    /**
     * 替换某一项（用于重复添加的情况）
     * TIP 子类可覆盖此方法
     * @param index  原始列表中的位置
     * @param newItem 新加的数据
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

    public int getOriginIndexOf(String id) {
        for (int i = 0; i<sizeOrigin(); i++) {
            T tmp = getOrigin(i);
            if (getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }
}
