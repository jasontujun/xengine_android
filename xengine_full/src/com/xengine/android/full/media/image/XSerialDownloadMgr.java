package com.xengine.android.full.media.image;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 图片线性下载管理类。
 * 封装了线性下载任务，以及相关操作。
 * 内部调用了XImageDownloadMgr来执行具体的下载操作。
 * @see XImageDownloadMgr
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public class XSerialDownloadMgr implements XSerial<XImageDownloadListener> {

    private XImageDownloadMgr mImageDownloadMgr;

    private SerialDownloadTask nextTask;
    private LinkedList<SerialDownloadTask> tobeExecuted;
    private boolean isDownloading;

    public XSerialDownloadMgr(XImageDownloadMgr imageDownloadMgr) {
        mImageDownloadMgr = imageDownloadMgr;
        tobeExecuted = new LinkedList<SerialDownloadTask>();
        isDownloading = false;
    }


    private void startTask(SerialDownloadTask task) {
        addNewTask(task);
        fireDownload();
    }


    @Override
    public void startTask(String imgUrl, XImageDownloadListener listener) {
        startTask(new SerialDownloadTask(imgUrl, listener));
    }


    private void startTasks(List<SerialDownloadTask> taskList) {
        for(SerialDownloadTask task : taskList) {
            addNewTask(task);
        }
        fireDownload();
    }


    @Override
    public void startTasks(List<String> imgUrlList, List<XImageDownloadListener> listenerList) {
        List<SerialDownloadTask> taskList = new ArrayList<SerialDownloadTask>();
        for(int i = 0; i<imgUrlList.size(); i++) {
            taskList.add(new SerialDownloadTask(imgUrlList.get(i), listenerList.get(i)));
        }
        startTasks(taskList);
    }

    /**
     * 判断是否包含某任务
     * @param id
     * @return
     */
    private boolean containsTask(String id) {
        Iterator<SerialDownloadTask> it = tobeExecuted.iterator();
        while (it.hasNext()) {
            if(it.next().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加新的下载任务
     * @param task
     */
    private synchronized boolean addNewTask(SerialDownloadTask task) {
        if(!containsTask(task.getId())) {// 判断是否重复
            tobeExecuted.offer(task);
            return true;
        }
        return false;
    }

    /**
     * 启动下载进程
     */
    private void fireDownload() {
        nextTask = tobeExecuted.peek();
        if(!isDownloading && nextTask != null) {
            nextTask.execute(null);
            isDownloading = true;
        }
    }


    @Override
    public void stop() {
        isDownloading = false;
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
     * 回调此函数。图片下载任务完成后，通知下载管理器执行下一个或停止。
     * @param task
     */
    private synchronized void notifyDownloadFinished(SerialDownloadTask task) {
        if(!isDownloading) {
            return;
        }

        tobeExecuted.remove(task);
        nextTask = tobeExecuted.peek();
        if(nextTask != null) {
            nextTask.execute(null);
        }else {
            isDownloading = false;
        }
    }




    /**
     * 单个线性下载任务。
     */
     private class SerialDownloadTask extends AsyncTask<Void, Void, String> {

        private String id;
        private XImageDownloadListener listener;

        public SerialDownloadTask(String id, XImageDownloadListener listener) {
            this.id = id;
            this.listener = listener;
        }

        public String getId() {
            return id;
        }

        @Override
        protected void onPreExecute() {
            if(listener != null)
                listener.onBeforeDownload(id);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return mImageDownloadMgr.downloadImg2File(id, 100);
        }

        @Override
        protected void onPostExecute(String result) {
            if(listener != null)
                listener.onFinishDownload(id, result);// 通知监听者

            notifyDownloadFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            notifyDownloadFinished(this);
        }
    }
}
