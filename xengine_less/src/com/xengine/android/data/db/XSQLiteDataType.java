package com.xengine.android.data.db;

/**
 * SQLite的五种数据类型
 * Created by 赵之韵.
 * Date: 11-12-15
 * Time: 下午9:41
 */
public enum XSQLiteDataType {

    /**
     * 字段的类型是空
     */
    NULL,

    /**
     * 字段的类型为整形
     */
    INTEGER,

    /**
     * 字段的类型为浮点数（以8字节存储）
     */
    REAL,

    /**
     * 字段的类型为字符串
     */
    TEXT,

    /**
     * 字段的类型为长整型
     */
    LONG,

    /**
     * 字段的类型为二进制
     */
    BLOB;
}
