package com.xengine.android.base.task;

/**
 * <pre>
 * 任务执行器的接口。
 * 每个任务执行类包含一个任务数据类，
 * B表示任务数据的类型，B继承自TaskBean。
 * User: jasontujun
 * Date: 13-9-27
 * Time: 上午9:34
 * </pre>
 */
public interface XTaskExecutor<B extends XTaskBean> {

    /**
     * 开始或继续下载。
     * @return 开始或继续下载是否成功
     */
    boolean start();

    /**
     * 暂停下载。
     * @return 暂停下载是否成功
     */
    boolean pause();

    /**
     * 终止并清除下载任务（删除相关内存和文件中的数据）。
     * @return 终止并清除下载任务是否成功
     */
    boolean abort();

    /**
     * 获取任务的数据bean
     * @return
     */
    B getBean();

    /**
     * 获取任务的唯一Id。
     * @return
     */
    String getId();

    /**
     * 设置下载任务的状态
     * @param status
     */
    void setStatus(int status);

    /**
     * 获取任务的状态。
     * @return
     */
    int getStatus();
}
