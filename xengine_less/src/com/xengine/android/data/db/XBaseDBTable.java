package com.xengine.android.data.db;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 继承BaseDBTable实现自定义数据表。
 * 在自定义数据表中应当以字符串常量的形式定义数据表中的字段名。
 * 在initiate函数中调用addColumn(name, type)方法将字段加入数据表中。
 * Created by 赵之韵.
 * Date: 11-12-15
 * Time: 下午8:18
 */
public abstract class XBaseDBTable<T> implements XDBTable<T> {

    /**
     * Constructor
     */
    public XBaseDBTable() {
        initiateColumns();
    }

    /**
     * 数据表中的字段名称
     */
    private ArrayList<String> columns = new ArrayList<String>();

    /**
     * 每个字段对应的类型
     */
    private ArrayList<XSQLiteDataType> types = new ArrayList<XSQLiteDataType>();

    /**
     * 每个字段对应的约束
     */
    private HashMap<String, XSQLiteConstraint> constraints = new HashMap<String, XSQLiteConstraint>();

    /**
     * 初始化字段，在本函数中通过addColumn(name, type)来增加字段。
     */
    public abstract void initiateColumns();

    @Override
    public synchronized String createTableString() {
        StringBuilder buf = new StringBuilder();
        buf.append("CREATE TABLE ")
                .append(getName())
                .append(" ")
                .append("(_id " + XSQLiteDataType.INTEGER + " PRIMARY KEY, ");
        for(int i = 0; i < columns.size() - 1; i++) {
            buf.append(columns.get(i))
                    .append(" ")
                    .append(types.get(i))
                    .append(" ");
            XSQLiteConstraint constraint = constraints.get(columns.get(i));
            if(constraint != null) {
                buf.append(constraint.toString().toUpperCase());
            }
            buf.append(", ");
        }
        int last = columns.size() - 1;
        buf.append(columns.get(last))
                .append(" ")
                .append(types.get(last))
                .append(" ");
        XSQLiteConstraint constraint = constraints.get(columns.get(last));
        if(constraint != null) {
            buf.append(constraint.toString().toUpperCase());
        }
        buf.append(");");
        return buf.toString();
    }

    @Override
    public synchronized void addColumn(String colName, XSQLiteDataType type, XSQLiteConstraint constraint) {
        if(colName == null || type == null) {
            throw new NullPointerException("Column name or type can not be null.");
        }
        columns.add(colName);
        types.add(type);
        if(constraint != null) {
            constraints.put(colName, constraint);
        }
    }

    @Override
    public synchronized String[] getColumns() {
        String[] cols = new String[columns.size()];
        return columns.toArray(cols);
    }
}
