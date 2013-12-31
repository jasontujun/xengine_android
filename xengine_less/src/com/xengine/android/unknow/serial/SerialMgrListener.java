package com.xengine.android.unknow.serial;

import com.xengine.android.unknow.task.TaskBean;
import com.xengine.android.unknow.task.TaskListener;

import java.util.List;

/**
 * <pre>
 * 线性任务执行器的监听类。
 * User: jasontujun
 * Date: 13-9-27
 * Time: 下午4:11
 * </pre>
 */
public interface SerialMgrListener<T extends TaskBean>
        extends TaskListener<T> {

    /**
     * 获取监听的唯一id
     * @return
     */
    String getId();

    /**
     * 添加任务后的回调函数（在UI线程）
     * @param task
     */
    void onAdd(T task);

    /**
     * 批量添加任务后的回调函数（在UI线程）
     * @param tasks 真正添加进队列的任务
     */
    void onAddAll(List<T> tasks);

    /**
     * 删除任务后的回调函数（在UI线程）
     * @param task
     */
    void onRemove(T task);

    /**
     * 批量删除任务后的回调函数（在UI线程）
     * @param tasks
     */
    void onRemoveAll(List<T> tasks);

    /**
     * 终止并清空所有下载任务
     */
    void onStopAndReset();
}
