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
 * Email: ttxzmorln@163.com
 * Date: 12-3-17
 * Time: 下午11:43
 */
public abstract class XBaseFilteredAdapterSource<T>
        implements XFilteredAdapterDataSource<T> {

    protected XFilter<T> mFilter;
    protected ArrayList<T> mItemList;
    protected ArrayList<T> mCache;
    protected ArrayList<XDataChangeListener<T>> mListeners;
    protected ArrayList<XDataChangeListener<T>> mOriginListeners;
    protected Comparator<T> mComparator;

    /**
     * 自动通知监听者
     */
    protected boolean isAutoNotify = true;

    public XBaseFilteredAdapterSource() {
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
        if (isAutoNotify)
            for (XDataChangeListener<T> listener: mListeners)
                listener.onChange();
    }

    @Override
    public void sortOrigin(Comparator<T> comparator) {
        mComparator = comparator;
        Collections.sort(mItemList, comparator);
        if (isAutoNotify)
            for (XDataChangeListener<T> listener: mOriginListeners) {
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

        if (isAutoNotify)
            for (XDataChangeListener<T> listener: mListeners)
                listener.onChange();
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
                for (XDataChangeListener<T> listener: mOriginListeners) {
                    listener.onAdd(item);
                }

            T result = mFilter.doFilter(item);
            if (result != null) {
                mCache.add(result);
                if (isAutoNotify)
                    for (XDataChangeListener<T> listener: mListeners) {
                        listener.onAdd(item);
                    }
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

                T result = mFilter.doFilter(item);
                if (result != null) {
                    mCache.add(result);
                    addedToCache.add(item);
                }
            }
        }
        if (isAutoNotify) {
            for (XDataChangeListener<T> listener: mOriginListeners) {
                listener.onAddAll(addedToOrigin);
            }
            for (XDataChangeListener<T> listener: mListeners) {
                listener.onAddAll(addedToCache);
            }
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
        if (item != null) {
            mCache.remove(item);
            mItemList.remove(item);
            if (isAutoNotify) {
                for(XDataChangeListener<T> listener: mOriginListeners) {
                    listener.onDelete(item);
                }
                for(XDataChangeListener<T> listener: mListeners) {
                    listener.onDelete(item);
                }
            }
        }
    }

    @Override
    public void delete(T item) {
        if (mCache.contains(item)) {
            mCache.remove(item);
            mItemList.remove(item);
            if (isAutoNotify) {
                for (XDataChangeListener<T> listener: mOriginListeners)
                    listener.onDelete(item);
                for (XDataChangeListener<T> listener: mListeners)
                    listener.onDelete(item);
            }
        }
    }

    @Override
    public void deleteAll(List<T> items) {
        ArrayList<T> deleted = new ArrayList<T>();
        for (T item: items) {
            if (mCache.contains(item)) {
                deleted.add(item);
                mCache.remove(item);
                mItemList.remove(item);
            }
        }
        if (isAutoNotify) {
            for (XDataChangeListener<T> listener: mOriginListeners) {
                listener.onDeleteAll(deleted);
            }
            for (XDataChangeListener<T> listener: mListeners) {
                listener.onDeleteAll(deleted);
            }
        }
    }

    @Override
    public void notifyDataChanged() {
        for (XDataChangeListener<T> listener: mOriginListeners)
            listener.onChange();
        for (XDataChangeListener<T> listener: mListeners)
            listener.onChange();
    }

    @Override
    public int indexOf(T item) {
        return mCache.indexOf(item);
    }

    @Override
    public void clear() {
        for (XDataChangeListener<T> listener: mOriginListeners)
            listener.onDeleteAll(mItemList);
        for (XDataChangeListener<T> listener: mListeners)
            listener.onDeleteAll(mCache);
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

    @Override
    public void setAutoNotifyListeners(boolean isAuto) {
        this.isAutoNotify = isAuto;
    }

}
