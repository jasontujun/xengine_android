package com.xengine.android.data.cache;

import com.xengine.android.base.filter.XFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 带过滤功能的数据源抽象类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-17
 * Time: 下午11:43
 */
public abstract class XBaseFilteredAdapterSource<T>
        implements XFilteredAdapterDataSource<T> {

    protected XFilter<T> mFilter;
    protected ArrayList<T> mItemList;
    protected ArrayList<T> mCache;
    protected List<XDataChangeListener<T>> mListeners;
    protected List<XDataChangeListener<T>> mOriginListeners;
    protected Comparator<T> mComparator;

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify = true;

    public XBaseFilteredAdapterSource() {
        mItemList = new ArrayList<T>();
        mCache = new ArrayList<T>();
        mListeners = new ArrayList<XDataChangeListener<T>>();
        mOriginListeners = new ArrayList<XDataChangeListener<T>>();
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
    public int size() {
        return mCache.size();
    }

    @Override
    public T getOrigin(int i) {
        return mItemList.get(i);
    }

    @Override
    public int sizeOrigin() {
        return mItemList.size();
    }

    @Override
    public void add(T item) {
        if (!mItemList.contains(item)) {
            mItemList.add(item);
            if (isAutoNotify)
                notifyAddOriginItem(item);

            T result = (mFilter != null) ? mFilter.doFilter(item) : item;
            if (result != null) {
                mCache.add(result);
                if (isAutoNotify)
                    notifyAddItem(item);
            }
        }
    }

    @Override
    public void addAll(List<T> items) {
        if(items == null) return;
        ArrayList<T> addedToOrigin = new ArrayList<T>();
        ArrayList<T> addedToCache = new ArrayList<T>();

        for (T item: items) {
            if (!mItemList.contains(item)) {
                mItemList.add(item);
                addedToOrigin.add(item);

                T result = (mFilter != null) ? mFilter.doFilter(item) : item;
                if (result != null) {
                    mCache.add(result);
                    addedToCache.add(item);
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
    public void delete(int index) {
        if (index < 0 || index >= mCache.size())
            return;

        T item = mCache.get(index);
        if (item != null)
            delete(item);
    }

    @Override
    public void delete(T item) {
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
    public void deleteAll(List<T> items) {
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
    public void notifyDataChanged() {
        notifyOriginDataChanged();
        notifyCacheDataChanged();
    }

    protected void notifyCacheDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = mListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onChange();
    }

    protected void notifyOriginDataChanged() {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onChange();
    }

    protected void notifyAddItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddOriginItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAdd(item);
    }

    protected void notifyAddItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyAddOriginItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onAddAll(items);
    }

    protected void notifyDeleteItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteOriginItem(T item) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDelete(item);
    }

    protected void notifyDeleteItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mListeners;
        for (XDataChangeListener<T> listener: finalListeners)
            listener.onDeleteAll(items);
    }

    protected void notifyDeleteOriginItems(List<T> items) {
        final List<XDataChangeListener<T>> finalListeners = mOriginListeners;
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
    public synchronized void registerDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(mListeners);
            if (!copyListeners.contains(listener)) {
                copyListeners.add(listener);
                mListeners = copyListeners;
            }
        }
    }

    @Override
    public synchronized void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(mListeners);
            if (copyListeners.remove(listener))
                mListeners = copyListeners;
        }
    }


    @Override
    public synchronized void registerDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(mOriginListeners);
            if (!copyListeners.contains(listener)) {
                copyListeners.add(listener);
                mOriginListeners = copyListeners;
            }
        }
    }

    @Override
    public synchronized void unregisterDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        if (listener != null) {
            // cow
            List<XDataChangeListener<T>> copyListeners = new ArrayList<XDataChangeListener<T>>(mOriginListeners);
            if (copyListeners.remove(listener))
                mOriginListeners = copyListeners;
        }
    }

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }

}
