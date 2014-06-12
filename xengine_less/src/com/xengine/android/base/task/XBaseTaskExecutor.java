package com.xengine.android.base.task;

/**
 * <pre>
 * 普通任务执行器的抽象类。
 * 封装了一个任务的核心状态机逻辑。
 * 注意:
 * 1.状态机：有四个状态TODO,DOING,DONE,ERROR
 *           和五个行为start,pause,abort,endSuccess,endError
 *      start = TODO/ERROR -> DOING
 *      pause = DOING -> TODO
 *      abort = TODO/DOING -> DONE
 *      endSuccess = DOING -> DONE
 *      endError = DOING -> ERROR
 * 2.子类继承时，重写五个行为的回调方法即可：
 *      onStart(),onPause(),onAbort(),onEndSuccess(),onEndError
 * User: jasontujun
 * Date: 13-9-27
 * Time: 上午10:03
 * </pre>
 */
public abstract class XBaseTaskExecutor<B extends XTaskBean>
        implements XTaskExecutor<B> {

    private B mBean;// 任务数据

    public XBaseTaskExecutor(B bean) {
        mBean = bean;
        setStatus(XTaskBean.STATUS_TODO);// 初识状态为TODO
    }

    @Override
    public B getBean() {
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
    public boolean start() {
        if (getStatus() != XTaskBean.STATUS_TODO
                && getStatus() != XTaskBean.STATUS_ERROR)
            return false;

        // 先修改状态，再调用自定义方法onStart
        int oldStatus = getStatus();
        setStatus(XTaskBean.STATUS_DOING);

        if (!onStart()) {
            setStatus(oldStatus);// 启动失败，恢复成以前的状态
            return false;
        }

        notifyStart(getBean());
        return true;
    }

    @Override
    public boolean pause() {
        if (getStatus() != XTaskBean.STATUS_DOING)
            return false;

        if (!onPause())
            return false;

        setStatus(XTaskBean.STATUS_TODO);
        notifyPause(getBean());
        return true;
    }

    @Override
    public boolean abort() {
        if (getStatus() != XTaskBean.STATUS_TODO
                && getStatus() != XTaskBean.STATUS_DOING)
            return false;

        if (!onAbort())
            return false;

        setStatus(XTaskBean.STATUS_DONE);
        notifyAbort(getBean());
        return true;
    }

    public boolean endSuccess() {
        if (getStatus() != XTaskBean.STATUS_DOING)
            return false;

        if (!onEndSuccess())
            return false;

        setStatus(XTaskBean.STATUS_DONE);
        notifyEndSuccess(getBean());
        return true;
    }

    public boolean endError(String errorCode, boolean retry) {
        if (getStatus() != XTaskBean.STATUS_DOING)
            return false;

        if (!onEndError(errorCode, retry))
            return false;

        setStatus(XTaskBean.STATUS_ERROR);
        notifyEndError(getBean(), errorCode, retry);
        return true;
    }

    /**
     * 启动任务的回调函数。
     * @return 启动成功返回true;否则返回false
     */
    protected abstract boolean onStart();

    /**
     * 暂停任务的回调函数。
     * @return 暂停成功返回true;否则返回false
     */
    protected abstract boolean onPause();

    /**
     * 终止任务的回调函数。
     * @return 终止成功返回true;否则返回false
     */
    protected abstract boolean onAbort();

    protected abstract boolean onEndSuccess();

    /**
     * 任务失败结束的回调。
     * @param errorCode 错误码
     * @param retry 如果需要重试，则为true；否则为false
     * @return 没有发生异常返回true;否则返回false
     */
    protected abstract boolean onEndError(String errorCode, boolean retry);

    /**
     * 通知外部任务已启动
     * @param bean
     */
    protected void notifyStart(B bean) {}

    /**
     * 通知外部任务已暂停
     * @param bean
     */
    protected void notifyPause(B bean) {}

    /**
     * 通知外部任务已终止
     * @param bean
     */
    protected void notifyAbort(B bean) {}

    /**
     * 通知外部任务正在执行
     * @param bean
     */
    protected void notifyDoing(B bean, long completeSize) {}

    /**
     * 通知外部任务成功结束
     * @param bean
     */
    protected void notifyEndSuccess(B bean) {}

    /**
     * 通知外部任务失败结束
     * @param bean
     */
    protected void notifyEndError(B bean, String errorCode, boolean retry) {}
}
