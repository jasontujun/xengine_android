package com.xengine.android.data.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.xengine.android.data.db.XDBTable;
import com.xengine.android.data.db.XSQLiteHelper;

/**
 * 继承自XBaseAdapterIdDataSource的带数据库支持的数据源抽象类。
 * Created by 赵之韵.
 * Email: ttxzmorln@163.com
 * Date: 12-3-8
 * Time: 下午7:29
 * @see com.xengine.android.data.cache.XBaseAdapterIdDataSource
 */
public abstract class XBaseAdapterIdDBDataSource<T>
        extends XBaseAdapterIdDataSource<T> implements XWithDatabase<T> {

    @Override
    public boolean addToDatabase() {
        XSQLiteHelper dbHelper = XSQLiteHelper.getInstance();
        XDBTable<T> table = getDatabaseTable();
        if (!dbHelper.isTableExist(table))
            dbHelper.createTable(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null)
            return false;
        for (int i = 0; i < size(); i++) {
            ContentValues cv = table.getContentValues(get(i));
            db.insert(table.getName(), null, cv);
        }
        db.close();
        return true;
    }

    @Override
    public boolean saveToDatabase() {
        XSQLiteHelper dbHelper = XSQLiteHelper.getInstance();
        XDBTable<T> table = getDatabaseTable();
        if (dbHelper.isTableExist(table)) {
            dbHelper.dropTable(table);
        }
        dbHelper.createTable(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null)
            return false;
        for (int i = 0; i < size(); i++) {
            ContentValues cv = table.getContentValues(get(i));
            db.insert(table.getName(), null, cv);
        }
        db.close();
        return true;
    }

    @Override
    public boolean loadFromDatabase() {
        XSQLiteHelper dbHelper = XSQLiteHelper.getInstance();
        XDBTable<T> table = getDatabaseTable();
        dbHelper.createIfNotExist(table);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db == null)
            return false;

        clear();
        Cursor cur = db.rawQuery("SELECT * FROM " + table.getName(), null);
        if (cur.moveToFirst()) {
            while (!cur.isAfterLast()) {
                T item = table.getFilledInstance(cur);
//                add(item);// 太低效
                mItemList.add(item);
                cur.moveToNext();
            }
        }
        cur.close();
        db.close();
        return true;
    }

}
