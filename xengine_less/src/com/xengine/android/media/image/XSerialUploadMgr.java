package com.xengine.android.media.image;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 图片线性上传管理类。
 * 单例模式。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public class XSerialUploadMgr implements XSerial<XImageUploadListener> {

    private SerialUploadTask nextTask;
    private LinkedList<SerialUploadTask> tobeExecuted;
    private boolean isUploading;

    public XSerialUploadMgr() {
        tobeExecuted = new LinkedList<SerialUploadTask>();
        isUploading = false;
    }


    private void startTask(SerialUploadTask task) {
        addNewTask(task);
        fireUpload();
    }


    @Override
    public void startTask(String imgUrl, XImageUploadListener listener) {
        startTask(new SerialUploadTask(imgUrl, listener));
    }


    private void startTasks(List<SerialUploadTask> taskList) {
        for(SerialUploadTask task : taskList) {
            addNewTask(task);
        }
        fireUpload();
    }


    @Override
    public void startTasks(List<String> imgUrlList, List<XImageUploadListener> listenerList) {
        List<SerialUploadTask> taskList = new ArrayList<SerialUploadTask>();
        for(int i = 0; i<imgUrlList.size(); i++) {
            taskList.add(new SerialUploadTask(imgUrlList.get(i), listenerList.get(i)));
        }
        startTasks(taskList);
    }

    /**
     * 判断是否包含某任务
     * @param id
     * @return
     */
    private boolean containsTask(String id) {
        Iterator<SerialUploadTask> it = tobeExecuted.iterator();
        while (it.hasNext()) {
            if(it.next().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加新的上传任务
     * @param task
     */
    private synchronized boolean addNewTask(SerialUploadTask task) {
        if(!containsTask(task.getId())) {// 判断是否重复
            tobeExecuted.offer(task);
            return true;
        }
        return false;
    }

    /**
     * 启动上传进程
     */
    private void fireUpload() {
        nextTask = tobeExecuted.peek();
        if(!isUploading && nextTask != null) {
            nextTask.execute(null);
            isUploading = true;
        }
    }


    @Override
    public void stop() {
        isUploading = false;
        if(nextTask!=null) {
            nextTask.cancel(true);
        }
        nextTask = null;
    }


    @Override
    public void stopAndReset() {
        stop();
        tobeExecuted.clear();
    }


    /**
     * 回调此函数。图片上传任务完成后，通知上传管理器执行下一个或停止。
     * @param task
     */
    private synchronized void notifyUploadFinished(SerialUploadTask task) {
        if(!isUploading) {
            return;
        }

        tobeExecuted.remove(task);
        nextTask = tobeExecuted.peek();
        if(nextTask != null) {
            nextTask.execute(null);
        }else {
            isUploading = false;
        }
    }




    /**
     * 单个线性上传任务。
     */
     private class SerialUploadTask extends AsyncTask<Void, Void, Integer> {

        private String id;
        private XImageUploadListener listener;

        public SerialUploadTask(String id, XImageUploadListener listener) {
            this.id = id;
            this.listener = listener;
        }

        public String getId() {
            return id;
        }

        @Override
        protected void onPreExecute() {
            if(listener != null)
                listener.onBeforeUpload(id);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if(listener != null)
                return listener.doUpload(id);
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(listener != null)
                listener.onFinishUpload(id, result);// 通知监听者

            notifyUploadFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            notifyUploadFinished(this);
        }
    }
}
