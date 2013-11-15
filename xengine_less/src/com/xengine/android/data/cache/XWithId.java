package com.xengine.android.data.cache;

import java.util.List;

/**
 * 用于基于元素Id的缓存
 * Created by jasontujun.
 * Date: 12-8-23
 * Time: 下午10:54
 */
public interface XWithId<T> {

    /**
     * 某一项的id
     * @param item
     * @return
     */
    String getId(T item);

    /**
     * 根据id获取一个元素
     * @param id
     * @return
     */
    T getById(String id);

    /**
     * 根据id删除一个元素
     * @param id
     */
    void deleteById(String id);

    /**
     * 根据id删除一个元素
     * @param ids
     */
    void deleteAllById(List<String> ids);

    /**
     * 替换某一项（用于重复添加的情况）
     * @param index 原始列表中的位置
     * @param newItem 新的条目
     */
    void replace(int index, T newItem);

    /**
     * 根据Id获取一个元素的坐标
     * @param id
     * @return
     */
    public int getIndexById(String id);
}
