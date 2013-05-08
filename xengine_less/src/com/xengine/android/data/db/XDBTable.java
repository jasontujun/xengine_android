package com.xengine.android.data.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 数据表接口。
 * Created by 赵之韵.
 * Date: 11-12-15
 * Time: 下午7:58
 */
public interface XDBTable<T> {

    /**
     * 返回数据表的表名
     */
    String getName();

    /**
     * 添加字段
     * @param colName 字段的名称
     * @param type 字段的类型
     * @param constraint 约束
     */
    void addColumn(String colName, XSQLiteDataType type, XSQLiteConstraint constraint);

    /**
     * 返回创建表达额SQL CREATE TABLE语句
     */
    String createTableString();

    /**
     * 返回表中所有的字段名称
     */
    String[] getColumns();

    /**
     * 根据丢过来的实例，返回一个包含所有属性内容的ContentValues。
     * @param instance 模型的实例
     */
    ContentValues getContentValues(T instance);

    /**
     * 从cursor中取出数据，并填到一个实例中去，然后返回这个实例。
     */
    T getFilledInstance(Cursor cursor);

}
