package com.xengine.android.media.image.download;

import android.os.AsyncTask;
import com.xengine.android.session.series.XBaseSerialMgr;
import com.xengine.android.session.series.XSerialDownloadListener;

/**
 * 线性下载图片管理类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public final class XImageSerialDownloadMgr
        extends XBaseSerialMgr<String, XSerialDownloadListener> {

    private XImageDownload mImageDownloadMgr;

    public XImageSerialDownloadMgr(XImageDownload imageDownloadMgr) {
        super();
        mImageDownloadMgr = imageDownloadMgr;
    }

    @Override
    protected AsyncTask createTask(String data, XSerialDownloadListener listener) {
        return new SerialDownloadTask(data, listener);
    }

    @Override
    protected String getTaskId(AsyncTask task) {
        return ((SerialDownloadTask)task).getId();
    }

    /**
     * 单个线性下载任务。
     */
     private class SerialDownloadTask extends AsyncTask<Void, Void, String> {

        private String mUrl;
        private XSerialDownloadListener mListener;

        public SerialDownloadTask(String url, XSerialDownloadListener listener) {
            mUrl = url;
            mListener = listener;
        }

        public String getId() {
            return mUrl;
        }

        @Override
        protected void onPreExecute() {
            if (mListener != null)
                mListener.beforeDownload(mUrl);
            mImageDownloadMgr.setDownloadListener(mListener);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return mImageDownloadMgr.downloadImg2File(mUrl, null);
        }

        @Override
        protected void onPostExecute(String result) {
            mImageDownloadMgr.setDownloadListener(null);
            if (mListener != null)
                mListener.afterDownload(mUrl);
            notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mListener != null)
                mListener.afterDownload(mUrl);
            notifyTaskFinished(this);
        }
    }
}
