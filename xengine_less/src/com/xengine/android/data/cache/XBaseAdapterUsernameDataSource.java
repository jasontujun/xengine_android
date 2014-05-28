package com.xengine.android.data.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于UserName分类的数据源抽象类。
 * Created by 赵之韵.
 * Date: 11-12-17
 * Time: 上午1:01
 */
public abstract class XBaseAdapterUsernameDataSource<T>
        extends XBaseAdapterDataSource<T> implements XWithUsername<T> {

    /**
     * 根据用户名获取元素
     * @param username
     * @return
     */
    @Override
    public List<T> getByUsername(String username) {
        List<T> result = new ArrayList<T>();
        for(int i = 0; i<size(); i++) {
            T item = get(i);
            if(getUsername(item).equals(username)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 根据用户名删除元素
     * @param username
     */
    @Override
    public synchronized void deleteByUsername(String username) {
        List<T> result = getByUsername(username);
        deleteAll(result);
    }
}
