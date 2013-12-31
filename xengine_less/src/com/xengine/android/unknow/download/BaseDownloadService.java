package com.xengine.android.unknow.download;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import com.xengine.android.unknow.serial.SerialMgrListener;
import com.xengine.android.unknow.serial.SerialTask;
import com.xengine.android.unknow.serial.TaskScheduler;
import com.xengine.android.unknow.serial.impl.SerialMgrImpl;
import com.xengine.android.unknow.serial.impl.SerialSpeedMonitor;
import com.xengine.android.unknow.task.TaskBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 封装了download核心逻辑的service抽象类。
 * 外部使用者需要继承该类，并实现createTask()等抽象方法后，
 * 才能使用Service来进行下载的相关操作。
 * User: jasontujun
 * Date: 13-9-23
 * Time: 上午11:31
 * </pre>
 */
public abstract class BaseDownloadService extends Service {
    private static final String TAG = "XDownloadService";

    // =============== intent中action的自定义类型 =============== //
    public static final String ACTION_ADD_DOWNLOAD =
            "tv.pps.module.download.add";// 添加下载任务
    public static final String ACTION_ADD_ALL_DOWNLOAD =
            "tv.pps.module.download.addAll";// 批量添加下载任务
    public static final String ACTION_REMOVE_DOWNLOAD =
            "tv.pps.module.download.remove";// 删除下载任务
    public static final String ACTION_REMOVE_ALL_DOWNLOAD =
            "tv.pps.module.download.removeAll";// 批量删除下载任务
    public static final String ACTION_START_DOWNLOAD =
            "tv.pps.module.download.start";// 启动下载
    public static final String ACTION_RESUME_DOWNLOAD =
            "tv.pps.module.download.resume";// 恢复下载
    public static final String ACTION_PAUSE_DOWNLOAD =
            "tv.pps.module.download.pause";// 暂停下载
    public static final String ACTION_PAUSE_DOWNLOAD_BY_TYPE =
            "tv.pps.module.download.stopByType";// 根据类型暂停下载
    public static final String ACTION_STOP_DOWNLOAD =
            "tv.pps.module.download.stop";// 停止下载
    public static final String ACTION_STOP_DOWNLOAD_BY_TYPE =
            "tv.pps.module.download.stopByType";// 根据类型停止下载
    public static final String ACTION_STOP_RESET =
            "tv.pps.module.download.stopAndReset";// 停止并清空下载任务
    public static final String ACTION_SET_SCHEDULER =
            "tv.pps.module.download.setScheduler";// 设置任务调度器
    public static final String ACTION_REGISTER_LISTENER =
            "tv.pps.module.download.registerListener";// 注册监听
    public static final String ACTION_UNREGISTER_LISTENER =
            "tv.pps.module.download.unregisterListener";// 卸载监听
    public static final String ACTION_SET_RUNNING_TASK =
            "tv.pps.module.download.setRunningTask";// 设置正在运行的任务
    // =============== intent中extra =============== //
    public static final String EXTRA_KEY_TASK_ID = "taskId";
    public static final String EXTRA_KEY_TASK_IDS = "taskIds";
    public static final String EXTRA_KEY_TASK_TYPE = "taskType";
    public static final String EXTRA_KEY_TASK_BEAN = "taskBean";
    public static final String EXTRA_KEY_TASK_BEANS = "taskBeans";
    public static final String EXTRA_KEY_TASK_SCHEDULER = "taskScheduler";
    public static final String EXTRA_KEY_LISTENER = "Listener";
    public static final String EXTRA_KEY_LISTENER_ID = "ListenerId";
    // =============== Messenger中回调类型 =============== //
    public static final int LISTENER_ON_ADD = 1;
    public static final int LISTENER_ON_ADD_ALL = 2;
    public static final int LISTENER_ON_REMOVE = 3;
    public static final int LISTENER_ON_REMOVE_ALL = 4;
    public static final int LISTENER_ON_STOP_AND_RESET = 5;
    public static final int LISTENER_ON_START = 6;
    public static final int LISTENER_ON_STOP = 7;
    public static final int LISTENER_ON_ABORT = 8;
    public static final int LISTENER_ON_DOWNLOADING = 9;
    public static final int LISTENER_ON_COMPLETE = 10;
    public static final int LISTENER_ON_ERROR = 11;
    public static final int LISTENER_ON_SPEED_UPDATE = 12;
    // =============== Messenger中回调函数的参数 =============== //
    public static final String LISTENER_KEY_BEAN = "bean";
    public static final String LISTENER_KEY_BEANS = "beans";
    public static final String LISTENER_KEY_POSITION = "position";
    public static final String LISTENER_KEY_LOCAL_PATH = "localPath";
    public static final String LISTENER_KEY_ERROR_STR = "errorStr";
    public static final String LISTENER_KEY_SPEED = "speed";

    protected SerialMgrImpl mDownloadSerialMgr;// 线性下载器

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化下载器
        mDownloadSerialMgr = new SerialMgrImpl();
        mDownloadSerialMgr.setSpeedMonitor(new SerialSpeedMonitor(mDownloadSerialMgr));
        // 注册Service对线性下载器的监听
        mDownloadSerialMgr.registerListener(new InnerListener());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) {
            handleIntent(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadSerialMgr.stopAndReset();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 处理activity对service的调用（UI线程）
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        // 添加下载任务
        if (ACTION_ADD_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            TaskBean bean = (TaskBean) bundle.getSerializable(EXTRA_KEY_TASK_BEAN);
            if (bean == null)
                return;
            preTaskAdd(bean);// 对子类的回调
            SerialTask task = createTask(bean);// 生成对应的task
            if (task != null)
                mDownloadSerialMgr.addTask(task);
        }
        // 批量添加下载任务
        else if (ACTION_ADD_ALL_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            List<? extends TaskBean> beans = (List<? extends TaskBean>)
                    bundle.getSerializable(EXTRA_KEY_TASK_BEANS);
            if (beans == null)
                return;
            List<TaskBean> taskBeans = new ArrayList<TaskBean>();
            List<SerialTask> tasks = new ArrayList<SerialTask>();
            for (TaskBean bean : beans) {
                taskBeans.add(bean);
                SerialTask task = createTask(bean);// 生成对应的task
                if (task != null)
                    tasks.add(task);
            }
            preTaskAddAll(taskBeans);// 对子类的回调
            mDownloadSerialMgr.addTasks(tasks);
        }
        // 删除下载任务
        else if (ACTION_REMOVE_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String id = bundle.getString(EXTRA_KEY_TASK_ID);
            preTaskRemove(id);// 对子类的回调
            mDownloadSerialMgr.removeTaskById(id);
        }
        // 批量删除下载任务
        else if (ACTION_REMOVE_ALL_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            List<String> ids = (List<String>)
                    bundle.getSerializable(EXTRA_KEY_TASK_IDS);
            preTaskRemoveAll(ids);// 对子类的回调
            mDownloadSerialMgr.removeTasksById(ids);
        }
        // 启动下载
        else if (ACTION_START_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                preTaskStart(id);// 对子类的回调
                mDownloadSerialMgr.start(id);
            } else {
                preTaskStart(null);// 对子类的回调
                mDownloadSerialMgr.start();
            }
        }
        // 恢复下载
        else if (ACTION_RESUME_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                preTaskResume(id);// 对子类的回调
                mDownloadSerialMgr.resume(id);
            } else {
                preTaskResume(null);// 对子类的回调
                mDownloadSerialMgr.resume();
            }
        }
        // 暂停下载
        else if (ACTION_PAUSE_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                preTaskPause(id);// 对子类的回调
                mDownloadSerialMgr.pause(id);
            } else {
                preTaskPause(null);// 对子类的回调
                mDownloadSerialMgr.pause();
            }
        }
        // 根据类型暂停下载
        else if (ACTION_PAUSE_DOWNLOAD_BY_TYPE.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            int type = bundle.getInt(EXTRA_KEY_TASK_TYPE);
            preTaskPauseByType(type);// 对子类的回调
            mDownloadSerialMgr.pauseByType(type);
        }
        // 停止下载
        else if (ACTION_STOP_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                preTaskStop(id);// 对子类的回调
                mDownloadSerialMgr.stop(id);
            } else {
                preTaskStop(null);// 对子类的回调
                mDownloadSerialMgr.stop();
            }
        }
        // 根据类型停止下载
        else if (ACTION_STOP_DOWNLOAD_BY_TYPE.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            int type = bundle.getInt(EXTRA_KEY_TASK_TYPE);
            preTaskStopByType(type);// 对子类的回调
            mDownloadSerialMgr.stopByType(type);
        }
        // 暂停并清空下载
        else if (ACTION_STOP_RESET.equals(action)) {
            preTaskStopAndReset();// 对子类的回调
            mDownloadSerialMgr.stopAndReset();
        }
        // 注册外部监听
        else if (ACTION_REGISTER_LISTENER.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String id = bundle.getString(EXTRA_KEY_LISTENER_ID);
            Messenger messenger = (Messenger) bundle.get(EXTRA_KEY_LISTENER);
            if (!TextUtils.isEmpty(id) && messenger != null)
                mDownloadSerialMgr.registerListener(new WrapperListener(id, messenger));
        }
        // 取消注册外部监听
        else if (ACTION_UNREGISTER_LISTENER.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String id = bundle.getString(EXTRA_KEY_LISTENER_ID);
            mDownloadSerialMgr.unregisterListener(id);
        }
        // 设置任务调度器
        else if (ACTION_SET_SCHEDULER.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            int schedulerType = bundle.getInt(EXTRA_KEY_TASK_SCHEDULER);
            TaskScheduler<TaskBean> scheduler = createScheduler(schedulerType);
            mDownloadSerialMgr.setTaskScheduler(scheduler);
        }
        // 设置当前运行的任务
        else if (ACTION_SET_RUNNING_TASK.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String id = bundle.getString(EXTRA_KEY_TASK_ID);
            preSetRunningTask(id);// 对子类的回调
            mDownloadSerialMgr.setRunningTask(id);
        }
        // 其他自定义Action，由子类来处理
        else {
            onHandleIntent(intent);
        }
    }

    /**
     * 根据DownloadBean.getType()生成对应类型的SerialDownloadTask
     * @param bean
     * @return 如果参数正确，则返回对应的SerialDownloadTask，否则返回null;
     */
    protected abstract SerialTask createTask(TaskBean bean);

    /**
     * 根据type类型生成对应的TaskScheduler
     * @param type
     * @return 如果没有符合类型的TaskScheduler，则返回null
     */
    protected abstract TaskScheduler<TaskBean> createScheduler(int type);

    /**
     * 处理Service外部发送来的Intent（除已定义的动作）。
     * 提供给子类新增自定义Intent的扩展性。
     * @param intent 外部传入的intent
     */
    protected abstract void onHandleIntent(Intent intent);

    // ========= 执行之前的回调函数，增加可扩展性 ========= //
    protected void preTaskAdd(TaskBean task) {}
    protected void preTaskAddAll(List<TaskBean> tasks) {}
    protected void preTaskRemove(String taskId) {}
    protected void preTaskRemoveAll(List<String> taskIds) {}
    protected void preTaskStart(String taskId) {}
    protected void preTaskResume(String taskId) {}
    protected void preTaskPause(String taskId) {}
    protected void preTaskPauseByType(int taskType) {}
    protected void preTaskStop(String taskId) {}
    protected void preTaskStopByType(int taskType) {}
    protected void preTaskStopAndReset() {}
    protected void preSetRunningTask(String taskId) {}
    // ========= 执行之后(通知外部监听之前)的回调函数，增加可扩展性 ========= //
    protected abstract void postTaskAdd(TaskBean task);
    protected abstract void postTaskAddAll(List<TaskBean> tasks);
    protected abstract void postTaskRemove(TaskBean task);
    protected abstract void postTaskRemoveAll(List<TaskBean> tasks);
    protected abstract void postTaskStopAndReset();
    protected abstract void postTaskStart(TaskBean task);
    protected abstract void postTaskStop(TaskBean task);
    protected abstract void postTaskAbort(TaskBean task);
    protected abstract void postTaskDownloading(TaskBean task, long position);
    protected abstract void postTaskComplete(TaskBean task, String localFilePath);
    protected abstract void postTaskError(TaskBean task, String errorStr);
    protected abstract void postTaskSpeedUpdate(TaskBean task, long speed);



    /**
     * DownloadService对下载器的监听，增加子类的可扩展性
     */
    private class InnerListener implements SerialMgrListener<TaskBean> {

        @Override
        public String getId() {
            return "tv.pps.module.download.core.download.InnerListener";
        }

        @Override
        public void onAdd(TaskBean task) {
            postTaskAdd(task);
        }

        @Override
        public void onAddAll(List<TaskBean> tasks) {
            postTaskAddAll(tasks);
        }

        @Override
        public void onRemove(TaskBean task) {
            postTaskRemove(task);
        }

        @Override
        public void onRemoveAll(List<TaskBean> tasks) {
            postTaskRemoveAll(tasks);
        }

        @Override
        public void onStopAndReset() {
            postTaskStopAndReset();
        }

        @Override
        public void onStart(TaskBean task) {
            postTaskStart(task);
        }

        @Override
        public void onStop(TaskBean task) {
            postTaskStop(task);
        }

        @Override
        public void onAbort(TaskBean task) {
            postTaskAbort(task);
        }

        @Override
        public void onDownloading(TaskBean task, long completeSize) {
            postTaskDownloading(task, completeSize);
        }

        @Override
        public void onComplete(TaskBean task, String localFilePath) {
            postTaskComplete(task, localFilePath);
        }

        @Override
        public void onError(TaskBean task, String errorStr) {
            postTaskError(task, errorStr);
        }

        @Override
        public void onSpeedUpdate(TaskBean task, long speed) {
            postTaskSpeedUpdate(task, speed);
        }
    }

    /**
     * 下载器监听的包装类，根据当前下载器状态通知外部监听者（Messenger类型）
     */
    private class WrapperListener implements SerialMgrListener<TaskBean> {

        private String id;
        private Messenger messenger;

        private WrapperListener(String id, Messenger messenger) {
            this.id = id;
            this.messenger = messenger;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void onAdd(TaskBean task) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ADD;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddAll(List<TaskBean> tasks) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ADD_ALL;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEANS, (Serializable) tasks);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRemove(TaskBean task) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_REMOVE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRemoveAll(List<TaskBean> tasks) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_REMOVE_ALL;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEANS, (Serializable) tasks);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStopAndReset() {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_STOP_AND_RESET;
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStart(TaskBean task) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_START;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop(TaskBean task) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_STOP;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAbort(TaskBean task) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ABORT;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloading(TaskBean task, long completeSize) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_DOWNLOADING;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putLong(LISTENER_KEY_POSITION, completeSize);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComplete(TaskBean task, String localFilePath) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_COMPLETE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putString(LISTENER_KEY_LOCAL_PATH, localFilePath);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(TaskBean task, String errorStr) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ERROR;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putString(LISTENER_KEY_ERROR_STR, errorStr);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSpeedUpdate(TaskBean task, long speed) {
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_SPEED_UPDATE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putLong(LISTENER_KEY_SPEED, speed);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
