package com.xengine.android.full.data.cache;

import com.xengine.android.full.data.db.XDBTable;
import com.xengine.android.full.data.db.XSQLiteHelper;

/**
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-8
 * Time: 下午7:12
 */
public interface XWithDatabase<T> {
    void saveToDatabase();
    void loadFromDatabase();
    XDBTable<T> getDatabaseTable();
    XSQLiteHelper getDatabaseHelper();
}
