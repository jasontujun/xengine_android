package com.xengine.android.unknow.download;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import tv.pps.module.download.core.serial.SerialMgrListener;
import tv.pps.module.download.core.serial.SerialTask;
import tv.pps.module.download.core.serial.TaskScheduler;
import tv.pps.module.download.core.serial.impl.SerialMgrImpl;
import tv.pps.module.download.core.serial.impl.SerialSpeedMonitor;
import tv.pps.module.download.core.task.TaskBean;

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

    private SerialMgrImpl mDownloadSerialMgr;// 线性下载器

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化下载器
        mDownloadSerialMgr = new SerialMgrImpl();
        mDownloadSerialMgr.setSpeedMonitor(new SerialSpeedMonitor(mDownloadSerialMgr));
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
            List<SerialTask> tasks = new ArrayList<SerialTask>();
            for (TaskBean bean : beans) {
                SerialTask task = createTask(bean);// 生成对应的task
                if (task != null)
                    tasks.add(task);
            }
            mDownloadSerialMgr.addTasks(tasks);
        }
        // 删除下载任务
        else if (ACTION_REMOVE_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            String id = bundle.getString(EXTRA_KEY_TASK_ID);
            mDownloadSerialMgr.removeTaskById(id);
        }
        // 批量删除下载任务
        else if (ACTION_REMOVE_ALL_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            List<String> ids = (List<String>)
                    bundle.getSerializable(EXTRA_KEY_TASK_IDS);
            mDownloadSerialMgr.removeTasksById(ids);
        }
        // 启动下载
        else if (ACTION_START_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                mDownloadSerialMgr.start(id);
            } else {
                mDownloadSerialMgr.start();
            }
        }
        // 恢复下载
        else if (ACTION_RESUME_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                mDownloadSerialMgr.resume(id);
            } else {
                mDownloadSerialMgr.resume();
            }
        }
        // 暂停下载
        else if (ACTION_PAUSE_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                mDownloadSerialMgr.pause(id);
            } else {
                mDownloadSerialMgr.pause();
            }
        }
        // 根据类型暂停下载
        else if (ACTION_PAUSE_DOWNLOAD_BY_TYPE.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            int type = bundle.getInt(EXTRA_KEY_TASK_TYPE);
            mDownloadSerialMgr.pauseByType(type);
        }
        // 停止下载
        else if (ACTION_STOP_DOWNLOAD.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String id = bundle.getString(EXTRA_KEY_TASK_ID);
                mDownloadSerialMgr.stop(id);
            } else {
                mDownloadSerialMgr.stop();
            }
        }
        // 根据类型停止下载
        else if (ACTION_STOP_DOWNLOAD_BY_TYPE.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            int type = bundle.getInt(EXTRA_KEY_TASK_TYPE);
            mDownloadSerialMgr.stopByType(type);
        }
        // 暂停并清空下载
        else if (ACTION_STOP_RESET.equals(action)) {
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

    // ========= 通知外部监听之前的下载相关的回调函数 ========= //
    protected abstract void onTaskAdd(TaskBean task);
    protected abstract void onTaskAddAll(List<TaskBean> tasks);
    protected abstract void onTaskRemove(TaskBean task);
    protected abstract void onTaskRemoveAll(List<TaskBean> tasks);
    protected abstract void onTaskStopAndReset();
    protected abstract void onTaskStart(TaskBean task);
    protected abstract void onTaskStop(TaskBean task);
    protected abstract void onTaskAbort(TaskBean task);
    protected abstract void onTaskDownloading(TaskBean task, long position);
    protected abstract void onTaskComplete(TaskBean task, String localFilePath);
    protected abstract void onTaskError(TaskBean task, String errorStr);
    protected abstract void onTaskSpeedUpdate(TaskBean task, long speed);

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
            onTaskAdd(task);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ADD;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddAll(List<TaskBean> tasks) {
            onTaskAddAll(tasks);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ADD_ALL;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEANS, (Serializable) tasks);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRemove(TaskBean task) {
            onTaskRemove(task);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_REMOVE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRemoveAll(List<TaskBean> tasks) {
            onTaskRemoveAll(tasks);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_REMOVE_ALL;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEANS, (Serializable) tasks);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStopAndReset() {
            onTaskStopAndReset();
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_STOP_AND_RESET;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStart(TaskBean task) {
            onTaskStart(task);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_START;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop(TaskBean task) {
            onTaskStop(task);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_STOP;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAbort(TaskBean task) {
            onTaskAbort(task);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ABORT;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloading(TaskBean task, long completeSize) {
            onTaskDownloading(task, completeSize);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_DOWNLOADING;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putLong(LISTENER_KEY_POSITION, completeSize);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComplete(TaskBean task, String localFilePath) {
            onTaskComplete(task, localFilePath);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_COMPLETE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putString(LISTENER_KEY_LOCAL_PATH, localFilePath);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(TaskBean task, String errorStr) {
            onTaskError(task, errorStr);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_ERROR;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putString(LISTENER_KEY_ERROR_STR, errorStr);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSpeedUpdate(TaskBean task, long speed) {
            onTaskSpeedUpdate(task, speed);
            try {
                Message msg = Message.obtain();
                msg.arg1 = LISTENER_ON_SPEED_UPDATE;
                Bundle bundle = new Bundle();
                bundle.putSerializable(LISTENER_KEY_BEAN, task);
                bundle.putLong(LISTENER_KEY_SPEED, speed);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
