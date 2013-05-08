package com.xengine.android.full.data.db;

/**
 * Created by 赵之韵.
 * Date: 12-2-26
 * Time: 上午1:52
 */
public enum XSQLiteConstraint {
    UNIQUE {
        @Override
        public String toString() {
            return "UNIQUE";
        }
    },
    PRIMARY_KEY_AUTOINCREMENT {
        @Override
        public String toString() {
            return "PRIMARY KEY AUTOINCREMENT";
        }
    },
    NOT_NULL {
        @Override
        public String toString() {
            return "NOT NULL";
        }
    }
}
