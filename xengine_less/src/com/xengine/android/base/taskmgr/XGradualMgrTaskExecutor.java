package com.xengine.android.base.taskmgr;

import com.xengine.android.base.speed.calc.XSpeedCalculator;
import com.xengine.android.base.task.XGradualTaskExecutor;
import com.xengine.android.base.task.XTaskBean;

import java.util.List;

/**
 * <pre>
 * 继承自XGradualTaskExecutor的抽象类。
 * XGradualMgrTaskExecutor代表可以添加进TaskMgr的任务。
 * 该方法添加了任务
 * 用于
 * </pre>
 */
public abstract class XGradualMgrTaskExecutor<B extends XTaskBean>
        extends XGradualTaskExecutor<B> implements XMgrTaskExecutor<B> {

    private XSpeedCalculator mSpeedCalculator;// 速度计算器
    private XTaskMgr<XMgrTaskExecutor<B>, B> mTaskMgr;// 任务管理器

    public XGradualMgrTaskExecutor(B bean) {
        super(bean);
    }

    @Override
    public void setTaskMgr(XTaskMgr<XMgrTaskExecutor<B>, B> taskMgr) {
        mTaskMgr = taskMgr;
    }

    @Override
    public XTaskMgr<XMgrTaskExecutor<B>, B> getTaskMgr() {
        return mTaskMgr;
    }

    @Override
    public void setSpeedCalculator(XSpeedCalculator speedCalculator) {
        mSpeedCalculator = speedCalculator;
    }

    @Override
    public XSpeedCalculator getSpeedCalculator() {
        return mSpeedCalculator;
    }

    @Override
    protected void notifyStart(B bean) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onStart(getBean());
    }

    @Override
    protected void notifyPause(B bean) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onStop(getBean());
    }

    @Override
    protected void notifyAbort(B bean) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onAbort(getBean());
    }

    @Override
    protected void notifyDoing(B bean, long completeSize) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onDoing(getBean(), completeSize);
    }

    @Override
    protected void notifyEndSuccess(B bean) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onComplete(getBean());

        if (getTaskMgr() != null)
            getTaskMgr().notifyTaskFinished(this, false);
    }

    @Override
    protected void notifyEndError(B bean, String errorStr, boolean retry) {
        List<XTaskMgrListener<B>> listeners = getTaskMgr().getListeners();
        if (listeners != null)
            for (XTaskMgrListener<B> listener : listeners)
                listener.onError(getBean(), errorStr);

        if (getTaskMgr() != null)
            getTaskMgr().notifyTaskFinished(this, retry);
    }
}