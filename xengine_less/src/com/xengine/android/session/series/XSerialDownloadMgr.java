package com.xengine.android.session.series;

import android.os.AsyncTask;
import com.xengine.android.session.download.XDownloadListener;
import com.xengine.android.session.download.XDownloadMgr;

/**
 * 线性下载管理类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public class XSerialDownloadMgr
        extends XBaseSerialMgr<XSerialDownloadMgr.DownloadParams, XDownloadListener> {

    private XDownloadMgr mDownloadMgr;

    public XSerialDownloadMgr(XDownloadMgr downloadMgr) {
        super();
        mDownloadMgr = downloadMgr;
    }

    @Override
    protected AsyncTask createTask(DownloadParams data, XDownloadListener listener) {
        return new SerialDownloadTask(data, listener);
    }

    @Override
    protected String getTaskId(AsyncTask task) {
        return ((SerialDownloadTask)task).getId();
    }

    public class DownloadParams {
        public String url;
        public String path;
        public String fileName;
    }


    /**
     * 单个线性下载任务。
     */
     private class SerialDownloadTask extends AsyncTask<Void, Void, Boolean> {

        private DownloadParams mParams;
        private XDownloadListener mListener;

        public SerialDownloadTask(DownloadParams param, XDownloadListener listener) {
            mParams = param;
            mListener = listener;
        }

        public String getId() {
            return mParams.url;
        }

        @Override
        protected void onPreExecute() {
            mDownloadMgr.setDownloadListener(mListener);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mDownloadMgr.download(mParams.url, mParams.path, mParams.fileName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDownloadMgr.setDownloadListener(null);
            notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            notifyTaskFinished(this);
        }
    }
}
