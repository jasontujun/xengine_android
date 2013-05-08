package com.xengine.android.data.cache;

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
public abstract class XBaseFilteredAdapterIdSource<T> implements XFilteredAdapterDataSource<T>, XWithId<T> {
    private static final String TAG = "morln.cache";

    protected XFilter<T> filter = new XNullFilter<T>();
    protected ArrayList<T> itemList = new ArrayList<T>();
    protected ArrayList<T> cache = new ArrayList<T>();
    protected ArrayList<XDataChangeListener<T>> listeners = new ArrayList<XDataChangeListener<T>>();
    protected ArrayList<XDataChangeListener<T>> originListeners = new ArrayList<XDataChangeListener<T>>();

    public XBaseFilteredAdapterIdSource() {
        doFilter();
    }

    @Override
    public void sort(Comparator<T> comparator) {
        Collections.sort(cache, comparator);
        notifyDataChanged();
    }

    @Override
    public void sortOrigin(Comparator<T> comparator) {
        Collections.sort(itemList, comparator);
        for(XDataChangeListener listener: originListeners) {
            listener.onChange();
        }
    }

    @Override
    public void setFilter(XFilter<T> filter) {
        this.filter = filter;
        doFilter();
    }

    @Override
    public XFilter<T> getFilter() {
        return filter;
    }

    @Override
    public void doFilter() {
        cache.clear();
        if(filter == null) {
            cache.addAll(itemList);
        }else {
            cache.addAll(filter.doFilter(itemList));
        }

        notifyDataChanged();
    }

    @Override
    public T get(int i) {
        return cache.get(i);
    }
    @Override
    public T getOrigin(int i) {
        return itemList.get(i);
    }

    @Override
    public T getById(String id) {
        int originIndex = getOriginIndexOf(id);
        if(originIndex != -1) {
            return itemList.get(originIndex);
        }
        return null;
    }

    @Override
    public int size() {
        return cache.size();
    }


    @Override
    public int sizeOrigin() {
        return itemList.size();
    }

    @Override
    public synchronized void add(T item) {
        if(item == null)
            return;

        int originIndex = getOriginIndexOf(getId(item));
        if(originIndex == -1) {
            itemList.add(item);
            for(XDataChangeListener listener: originListeners) {
                listener.onAdd(item);
            }

            T result = filter.doFilter(item);
            if(result != null) {
                cache.add(result);
                for(XDataChangeListener listener: listeners) {
                    listener.onAdd(item);
                }
            }
        }else {
            replace(originIndex, item);
            for(XDataChangeListener listener: originListeners) {
                listener.onChange();
            }

            T result = filter.doFilter(item);
            if(result != null) {
                int index = getIndexById(getId(item));
                if(index == -1) {
                    cache.add(result);
                    for(XDataChangeListener listener: listeners) {
                        listener.onAdd(item);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void addAll(List<T> items) {
        if(items == null || items.size() == 0)
            return;

        ArrayList<T> addedToOrigin = new ArrayList<T>();
        ArrayList<T> addedToCache = new ArrayList<T>();

        for(T item: items) {
            int originIndex = getOriginIndexOf(getId(item));
            if(originIndex == -1) {
                itemList.add(item);
                addedToOrigin.add(item);

                T result = filter.doFilter(item);
                if(result != null) {
                    cache.add(result);
                    addedToCache.add(item);
                }
            }else {
                replace(originIndex, item);

                T result = filter.doFilter(item);
                if(result != null) {
                    int index = getIndexById(getId(item));
                    if(index == -1) {
                        cache.add(result);
                        addedToCache.add(item);
                    }
                }
            }
        }
        for(XDataChangeListener listener: originListeners) {
            listener.onAddAll(addedToOrigin);
        }
        for(XDataChangeListener listener: listeners) {
            listener.onAddAll(addedToCache);
        }
    }

    @Override
    public boolean isEmpty() {
        return cache.size() == 0;
    }

    @Override
    public synchronized void deleteById(String id) {
        int originIndex = getOriginIndexOf(id);
        if(originIndex != -1) {
            T item = itemList.remove(originIndex);
            for(XDataChangeListener listener: originListeners) {
                listener.onDelete(item);
            }

            int index = getIndexById(id);
            if(index != -1) {
                cache.remove(index);
                for(XDataChangeListener listener: listeners) {
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
        if(index < 0 || index >= cache.size())
            return;

        T item = cache.get(index);
        if(item != null) {
            cache.remove(item);
            itemList.remove(item);
            for(XDataChangeListener listener: originListeners) {
                listener.onDelete(item);
            }
            for(XDataChangeListener listener: listeners) {
                listener.onDelete(item);
            }
        }
    }

    @Override
    public void deleteAll(List<T> items) {
        ArrayList<T> deleted = new ArrayList<T>();
        for(T item: items) {
            String id = getId(item);
            int originIndex = getOriginIndexOf(id);
            if(originIndex != -1) {
                T deletedItem = itemList.remove(originIndex);
                int index = getIndexById(id);
                if(index != -1) {
                    cache.remove(index);
                }
                deleted.add(deletedItem);
            }
        }
        for(XDataChangeListener listener: originListeners) {
            listener.onDeleteAll(deleted);
        }
        for(XDataChangeListener listener: listeners) {
            listener.onDeleteAll(deleted);
        }
    }

    @Override
    public synchronized void deleteAllById(List<String> ids) {
        List<T> items = new ArrayList<T>();
        for(int i = 0; i < ids.size(); i++) {
            T item = getById(ids.get(i));
            if(item != null) {
                items.add(item);
            }
        }
        deleteAll(items);
    }

    @Override
    public void notifyDataChanged() {
        for(XDataChangeListener listener: listeners) {
            listener.onChange();
        }
    }

    @Override
    public int indexOf(T item) {
        return cache.indexOf(item);
    }

    @Override
    public void clear() {
        cache.clear();
        itemList.clear();
    }

    @Override
    public boolean contains(T item) {
        return cache.contains(item);
    }

    @Override
    public List<T> copyAll() {
        return new ArrayList<T>(cache);
    }

    @Override
    public void registerDataChangeListener(XDataChangeListener<T> listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterDataChangeListener(XDataChangeListener<T> listener) {
        listeners.remove(listener);
    }


    @Override
    public void registerDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        if(!originListeners.contains(listener)) {
            originListeners.add(listener);
        }
    }

    @Override
    public void unregisterDataChangeListenerForOrigin(XDataChangeListener<T> listener) {
        originListeners.remove(listener);
    }

    /**
     * 替换某一项（用于重复添加的情况）
     * TIP 子类可覆盖此方法
     * @param index  原始列表中的位置
     * @param newItem
     */
    @Override
    public void replace(int index, T newItem) {
        itemList.set(index, newItem);
    }

    @Override
    public int getIndexById(String id) {
        for(int i = 0; i<size(); i++) {
            T tmp = get(i);
            if(getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public int getOriginIndexOf(String id) {
        for(int i = 0; i<sizeOrigin(); i++) {
            T tmp = get(i);
            if(getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
