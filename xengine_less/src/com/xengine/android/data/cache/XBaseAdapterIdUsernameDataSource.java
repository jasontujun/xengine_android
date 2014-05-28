package com.xengine.android.data.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于UserName分类、基于Id唯一标识每个数据的数据源抽象类。
 * Created by jasontujun.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterIdUsernameDataSource<T>
        extends XBaseAdapterIdDataSource<T> implements XWithUsername<T> {

    public int getIndexByUsernameId(String username, String id) {
        for (int i = 0; i < size(); i++) {
            T tmp = get(i);
            if (getUsername(tmp).equals(username) &&
                    getId(tmp).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public synchronized void add(T item) {
        if (item == null)
            return;

        int index = getIndexByUsernameId(getUsername(item), getId(item));
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

        for (T item : items) {
            int index = getIndexByUsernameId(getUsername(item), getId(item));
            if (index == -1) {
                mItemList.add(item);
            } else {
                replace(index, item);
            }
        }
        if (mIsAutoNotify)
            notifyAddItems(items);
    }


    /**
     * 根据用户名获取元素
     * @param username
     * @return
     */
    @Override
    public List<T> getByUsername(String username) {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i<size(); i++) {
            T item = get(i);
            if (getUsername(item).equals(username)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 根据用户名和id获取元素
     * @param username
     * @param id
     * @return
     */
    public T getByUsernameId(String username, String id) {
        int index = getIndexByUsernameId(username, id);
        if (index != -1) {
            return get(index);
        }
        return null;
    }

    /**
     * 根据用户名删除一堆元素
     * @param username
     */
    @Override
    public synchronized void deleteByUsername(String username) {
        List<T> result = getByUsername(username);
        deleteAll(result);
    }


    /**
     * 根据用户名和Id删除元素
     * @param username
     * @param id
     */
    public synchronized void deleteByUsernameId(String username, String id) {
        int index = getIndexByUsernameId(username, id);
        if (index != -1)
            delete(index);
    }

    /**
     * 根据用户名和一堆Id删除一堆元素
     * @param username
     * @param ids
     */
    public synchronized void deleteAllByUsernameId(String username, List<String> ids) {
        if (ids == null || ids.size() == 0)
            return;
        List<T> result = new ArrayList<T>();
        for (String id : ids) {
            T item = getByUsernameId(username, id);
            if (item != null) {
                result.add(item);
            }
        }
        deleteAll(result);
    }
}
