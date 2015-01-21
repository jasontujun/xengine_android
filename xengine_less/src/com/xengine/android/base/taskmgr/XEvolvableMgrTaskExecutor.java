package com.xengine.android.base.taskmgr;

import com.xengine.android.base.speed.calc.XSpeedCalculator;
import com.xengine.android.base.task.XBaseTaskExecutor;
import com.xengine.android.base.task.XEvolvableTaskExecutor;
import com.xengine.android.base.task.XTaskBean;

/**
 * <pre>
 * 继承自XEvolvableTaskExecutor的抽象类。
 * XEvolvableMgrTaskExecutor代表可以添加进TaskMgr的任务。
 * User: jasontujun
 * Date: 15-1-19
 * Time: 下午5:08
 * </pre>
 */
public abstract class XEvolvableMgrTaskExecutor<B extends XTaskBean>
        extends XEvolvableTaskExecutor<B> implements XMgrTaskExecutor<B>  {

    private XSpeedCalculator mSpeedCalculator;// 速度计算器
    private XTaskMgr<XMgrTaskExecutor<B>, B> mTaskMgr;// 任务管理器

    public XEvolvableMgrTaskExecutor(B bean) {
        super(bean);
    }

    public XEvolvableMgrTaskExecutor(B bean, int status) {
        super(bean, status);
    }

    @Override
    protected synchronized void evolve(XBaseTaskExecutor<B> evolvedTask) {
        super.evolve(evolvedTask);
        if (evolvedTask instanceof XMgrTaskExecutor) {
            ((XMgrTaskExecutor) evolvedTask).setTaskMgr(mTaskMgr);
            ((XMgrTaskExecutor) evolvedTask).setSpeedCalculator(mSpeedCalculator);
        }
    }

    @Override
    public void setTaskMgr(XTaskMgr<XMgrTaskExecutor<B>, B> taskMgr) {
        mTaskMgr = taskMgr;
        XBaseTaskExecutor<B> evolvedTask = getEvolvedTask();
        if (evolvedTask != null && evolvedTask instanceof XMgrTaskExecutor) {
            ((XMgrTaskExecutor) evolvedTask).setTaskMgr(taskMgr);
        }
    }

    @Override
    public XTaskMgr<XMgrTaskExecutor<B>, B> getTaskMgr() {
        return mTaskMgr;
    }

    @Override
    public void setSpeedCalculator(XSpeedCalculator speedCalculator) {
        mSpeedCalculator = speedCalculator;
        XBaseTaskExecutor<B> evolvedTask = getEvolvedTask();
        if (evolvedTask != null && evolvedTask instanceof XMgrTaskExecutor) {
            ((XMgrTaskExecutor) evolvedTask).setSpeedCalculator(speedCalculator);
        }
    }

    @Override
    public XSpeedCalculator getSpeedCalculator() {
        return mSpeedCalculator;
    }
}
