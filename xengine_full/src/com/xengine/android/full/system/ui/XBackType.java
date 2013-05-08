package com.xengine.android.full.system.ui;

/**
 * Created by jasontujun.
 * Date: 12-4-5
 * Time: 下午4:26
 */
public class XBackType {
    /**
     * 返回失败
     */
    public static final int BACK_FAILED = 0;

    /**
     * View的内部执行返回（不影响外部状态）
     */
    public static final int CHILD_BACK = 1;

    /**
     * View自己返回（影响外部状态）
     */
    public static final int SELF_BACK = 2;

    /**
     * View不需要执行返回操作
     */
    public static final int NOTHING_TO_BACK = 3;
}
