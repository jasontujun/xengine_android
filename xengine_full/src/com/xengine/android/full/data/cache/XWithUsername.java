package com.xengine.android.full.data.cache;

import java.util.List;

/**
 * 基于多账号的缓存
 * Created by jasontujun.
 * Date: 12-9-12
 * Time: 下午9:00
 */
public interface XWithUsername<T> {

    /**
     * 某一项的所属用户名
     * @param item
     * @return
     */
    String getUsername(T item);

    /**
     * 根据用户名获取元素
     * @param username
     * @return
     */
    List<T> getByUsername(String username);

    /**
     * 根据用户名删除元素
     * @param username
     */
    void deleteByUsername(String username);
}
