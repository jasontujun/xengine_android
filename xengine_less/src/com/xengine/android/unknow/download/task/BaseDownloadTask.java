package com.xengine.android.unknow.download.task;

import com.xengine.android.unknow.serial.SerialMgr;
import com.xengine.android.unknow.serial.SerialMgrListener;
import com.xengine.android.unknow.serial.SerialTask;
import com.xengine.android.unknow.speed.calc.SpeedCalculator;
import com.xengine.android.unknow.task.TaskBean;

import java.util.List;

/**
 * <pre>
 * 实现SerialDownloadTask接口的基础任务类。
 * 包含了DownloadBean作为数据，使其具备在异步线程的执行能力。
 * 注意下载任务的"状态机"，start(),stop(),abort()三个方法
 * 以及异步线程的正常结束和错误结束，都是完成了不同状态间的跳转。
 * User: jasontujun
 * Date: 13-9-27
 * Time: 上午10:03
 * </pre>
 */
public abstract class BaseDownloadTask implements SerialTask {

    private TaskBean mBean;// 下载任务的数据
    private SpeedCalculator mSpeedCalculator;// 速度计算器
    private SerialMgr<SerialTask, TaskBean> mSerialMgr;// 线性管理器

    public BaseDownloadTask(TaskBean bean) {
        mBean = bean;
        setStatus(TaskBean.STATUS_TODO);
    }

    @Override
    public TaskBean getBean() {
        return mBean;
    }

    @Override
    public String getId() {
        return mBean.getId();
    }

    @Override
    public void setStatus(int status) {
        mBean.setStatus(status);
    }

    @Override
    public int getStatus() {
        return mBean.getStatus();
    }

    @Override
    public void setSerialMgr(SerialMgr<SerialTask, TaskBean> serialMgr) {
        mSerialMgr = serialMgr;
    }

    @Override
    public SerialMgr<SerialTask, TaskBean> getSerialMgr() {
        return mSerialMgr;
    }

    @Override
    public void setSpeedCalculator(SpeedCalculator speedCalculator) {
        mSpeedCalculator = speedCalculator;
    }

    @Override
    public SpeedCalculator getSpeedCalculator() {
        return mSpeedCalculator;
    }

    @Override
    public boolean start() {
        return getStatus() == TaskBean.STATUS_TODO
                || getStatus() == TaskBean.STATUS_ERROR;
    }

    @Override
    public boolean pause() {
        return getStatus() == TaskBean.STATUS_DOING;
    }

    @Override
    public boolean abort() {
        return getStatus() == TaskBean.STATUS_TODO
                || getStatus() == TaskBean.STATUS_DOING;
    }

    protected void onStart() {
        setStatus(TaskBean.STATUS_DOING);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onStart(getBean());
    }

    protected void onStop() {
        setStatus(TaskBean.STATUS_TODO);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onStop(getBean());
    }

    protected void onAbort() {
        setStatus(TaskBean.STATUS_DONE);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onAbort(getBean());
    }

    protected void onDownloading(long completeSize) {
//        setStatus(TaskBean.STATUS_DOING);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onDownloading(getBean(), completeSize);
    }

    protected void onEndError(String errorStr) {
        setStatus(TaskBean.STATUS_ERROR);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onError(getBean(), errorStr);

        if (getSerialMgr() != null)
            getSerialMgr().notifyTaskFinished(this);
    }

    protected void onEndRetry(String errorStr) {
        setStatus(TaskBean.STATUS_TODO);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onError(getBean(), errorStr);

        if (getSerialMgr() != null)
            getSerialMgr().notifyTaskFinished(this);
    }

    protected void onEndSuccess(String localPath) {
        setStatus(TaskBean.STATUS_DONE);
        List<SerialMgrListener<TaskBean>> listeners = getSerialMgr().getListeners();
        for (int i = 0; i<listeners.size(); i++)
            listeners.get(i).onComplete(getBean(), localPath);

        if (getSerialMgr() != null)
            getSerialMgr().notifyTaskFinished(this);
    }
}
