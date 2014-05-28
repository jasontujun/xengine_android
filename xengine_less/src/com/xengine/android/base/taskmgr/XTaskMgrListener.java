package com.xengine.android.base.taskmgr;

import com.xengine.android.base.task.XTaskBean;
import com.xengine.android.base.task.XTaskListener;

import java.util.List;

/**
 * <pre>
 * 任务管理器监听接口，继承自TaskListener接口
 * User: jasontujun
 * Date: 13-9-27
 * Time: 下午4:11
 * </pre>
 */
public interface XTaskMgrListener<T extends XTaskBean>
        extends XTaskListener<T> {

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

    /**
     * 所有任务都暂停。
     * 由于remove、stop等操作导致的，会触发此回调。
     * 由于pause导致的，不会触发此回调。
     */
    void onStopAll();

    /**
     * 完成所有下载任务
     */
    void onFinishAll();
}
