package com.xengine.android.full.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 数据库管理帮助类
 * Created by 赵之韵.
 * Date: 11-12-7
 * Time: 下午5:02
 */
public class XSQLiteHelper extends SQLiteOpenHelper{
    private static XSQLiteHelper instance;

    public static XSQLiteHelper getInstance() {
        return instance;
    }

    public static void initiate(Context context, String dbName, int dbVersion) {
        instance = new XSQLiteHelper(context, dbName, dbVersion);
    }

    /**
     * SQLite系统维护的表，记录数据库相关属性信息。
     */
    private static final String SYSTEM_TABLE = "sqlite_master";

    /**
     * 系统表中记录数据表名称的字段名称。
     */
    private static final String TABLE_NAME = "name";

    /**
     * 缓存系统中已经创建的表的名称
     */
    private List<String> tables = new ArrayList<String>();

    /**
     * 初始化SQLiteHelper类
     * @param context Context
     * @param dbName 数据库名称
     * @param dbVersion 数据库的版本
     */
    private XSQLiteHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 数据表的创建转移到createTable(table)中
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 暂且不进行升级的操作
    }

    /**
     * 查询数据库中是否已经存在name所对应的数据表，否则创建。
     * @param table 数据表对象
     */
    public boolean isTableExist(XDBTable table) {
        // 直接查询内部缓存的名称，如果已经存在就没必要去查询数据库了。
        if(tables.contains(table.getName())) {
            return true;
        }

        SQLiteDatabase db = getWritableDatabase();
        // TIP sqlite_master数据表是sqlite数据库维护的系统数据表
        Cursor cur =
                db.query(SYSTEM_TABLE, new String[] {TABLE_NAME},
                        TABLE_NAME + " = " + "'" + table.getName() + "'",
                        null, null, null, null);
        if(cur.moveToFirst()) {
            // 记得cursor使用之后一定要close
            cur.close();
            db.close();
            return true;
        }else {
            cur.close();
            db.close();
            return false;
        }
    }

    /**
     * 在数据库中创建一张新表
     * @param table 要创建的数据表
     */
    public void createTable(XDBTable table) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(table.createTableString());
        db.close();
    }

    /**
     * 删除数据表
     * @param table 要删除的数据表对象
     */
    public void dropTable(XDBTable table) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE " + table.getName());
        db.close();
    }

    /**
     * 如果数据表在数据库中不存在，则创建这个数据表
     */
    public void createIfNotExist(XDBTable table) {
        if(!isTableExist(table)) {
            createTable(table);
        }
    }

    /**
     * 数据表是否为空
     * @param table
     * @return
     */
    public boolean isTableEmpty(XDBTable table) {
        if(!isTableExist(table)) {
            return false;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + table.getName(), null);
        boolean result = (cur.getCount() == 0);

        cur.close();
        db.close();
        return result;
    }
}
