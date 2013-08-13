package com.xengine.android.data.cache;

import com.xengine.android.data.cache.filter.XFilter;
import com.xengine.android.data.cache.filter.XNullFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * FIXME, 合理安排onchange的调用次数，提高性能
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-17
 * Time: 下午11:43
 */
public abstract class XBaseFilteredAdapterIdSource<T>
        implements XFilteredAdapterDataSource<T>, XWithId<T> {

    protected XFilter<T> mFilter = new XNullFilter<T>();
    protected ArrayList<T> mItemList = new ArrayList<T>();
    protected ArrayList<T> mCache = new ArrayList<T>();
    protected ArrayList<XDataChangeListener<T>> mListeners = new ArrayList<XDataChangeListener<T>>();
    protected ArrayList<XDataChangeListener<T>> mOriginListeners = new ArrayList<XDataChangeListener<T>>();
    protected Comparator<T> mComparator;

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify = true;

    public XBaseFilteredAdapterIdSource() {
        mFilter = new XNullFilter<T>();
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
        notifyDataChanged();
    }

    @Override
    public void sortOrigin(Comparator<T> comparator) {
        mComparator = comparator;
        Collections.sort(mItemList, comparator);
        if (isAutoNotify)
            for (XDataChangeListener listener: mOriginListeners) {
                listener.onChange();
            }
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
        if (mFilter == null) {
            mCache.addAll(mItemList);
        } else {
            mCache.addAll(mFilter.doFilter(mItemList));
        }
        if (mComparator != null) {
            Collections.sort(mCache, mComparator);
            Collections.sort(mItemList, mComparator);
        }

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
        if (originIndex != -1) {
            return mItemList.get(originIndex);
        }
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
                for (XDataChangeListener listener: mOriginListeners) {
                    listener.onAdd(item);
                }

            T result = mFilter.doFilter(item);
            if (result != null) {
                mCache.add(result);
                if (isAutoNotify)
                    for (XDataChangeListener listener: mListeners) {
                        listener.onAdd(item);
                    }
            }
        } else {
            replace(originIndex, item);
            if (isAutoNotify)
                for (XDataChangeListener listener: mOriginListeners) {
                    listener.onChange();
                }

            T result = mFilter.doFilter(item);
            if (result != null) {
                int index = getIndexById(getId(item));
                if (index == -1) {
                    mCache.add(result);
                    if (isAutoNotify)
                        for (XDataChangeListener listener: mListeners) {
                            listener.onAdd(item);
                        }
                }
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

                T result = mFilter.doFilter(item);
                if (result != null) {
                    mCache.add(result);
                    addedToCache.add(item);
                }
            } else {
                replace(originIndex, item);

                T result = mFilter.doFilter(item);
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
            for (XDataChangeListener listener: mOriginListeners) {
                listener.onAddAll(addedToOrigin);
            }
            for (XDataChangeListener listener: mListeners) {
                listener.onAddAll(addedToCache);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return mCache.size() == 0;
    }

    @Override
    public synchronized void deleteById(String id) {
        int originIndex = getOriginIndexOf(id);
        if (originIndex != -1) {
            T item = mItemList.remove(originIndex);
            if (isAutoNotify)
                for (XDataChangeListener listener: mOriginListeners) {
                    listener.onDelete(item);
                }

            int index = getIndexById(id);
            if (index != -1) {
                mCache.remove(index);
                if (isAutoNotify)
                    for (XDataChangeListener listener: mListeners) {
                        listener.onDelete(item);
                    }
            }
        }
    }

    @Override
    public synchronized void delete(T item) {
        deleteById(getId(item));
    }

    @Override
    public synchronized void delete(int index) {
        if (index < 0 || index >= mCache.size())
            return;

        T item = mCache.get(index);
        if (item != null) {
            mCache.remove(item);
            mItemList.remove(item);
            if (isAutoNotify) {
                for (XDataChangeListener listener: mOriginListeners) {
                    listener.onDelete(item);
                }
                for (XDataChangeListener listener: mListeners) {
                    listener.onDelete(item);
                }
            }
        }
    }

    @Override
    public void deleteAll(List<T> items) {
        ArrayList<T> deleted = new ArrayList<T>();
        for (T item: items) {
            String id = getId(item);
            int originIndex = getOriginIndexOf(id);
            if (originIndex != -1) {
                T deletedItem = mItemList.remove(originIndex);
                int index = getIndexById(id);
                if (index != -1) {
                    mCache.remove(index);
                }
                deleted.add(deletedItem);
            }
        }
        if (isAutoNotify) {
            for (XDataChangeListener listener: mOriginListeners) {
                listener.onDeleteAll(deleted);
            }
            for (XDataChangeListener listener: mListeners) {
                listener.onDeleteAll(deleted);
            }
        }
    }

    @Override
    public synchronized void deleteAllById(List<String> ids) {
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
    public void notifyDataChanged() {
        if (isAutoNotify)
            for (XDataChangeListener listener: mListeners) {
                listener.onChange();
            }
    }

    @Override
    public int indexOf(T item) {
        return mCache.indexOf(item);
    }

    @Override
    public void clear() {
        mCache.clear();
        mItemList.clear();
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
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    @Override
    public void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        mListeners.remove(listener);
    }


    @Override
    public void registerDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        if (!mOriginListeners.contains(listener)) {
            mOriginListeners.add(listener);
        }
    }

    @Override
    public void unregisterDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        mOriginListeners.remove(listener);
    }

    /**
     * 替换某一项（用于重复添加的情况）
     * TIP 子类可覆盖此方法
     * @param index  原始列表中的位置
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

    public int getOriginIndexOf(String id) {
        for (int i = 0; i<sizeOrigin(); i++) {
            T tmp = get(i);
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
