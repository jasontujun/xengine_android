package com.xengine.android.system.upload;

import android.os.AsyncTask;
import com.xengine.android.system.series.XWrapperSerialMgr;

import java.io.File;
import java.util.Map;

/**
 * 线性上传管理类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public final class XSerialUploadMgr extends
        XWrapperSerialMgr<XSerialUploadMgr.UploadParams, XSerialUploadListener> {

    private XUpload mUploadMgr;

    public XSerialUploadMgr(XUpload uploadMgr) {
        super();
        mUploadMgr = uploadMgr;
    }

    @Override
    protected AsyncTask createTask(UploadParams data, XSerialUploadListener listener) {
        return new SerialUploadTask(data, listener);
    }

    @Override
    public String getTaskId(AsyncTask task) {
        return ((SerialUploadTask)task).getId();
    }

    public class UploadParams {
        public String url;
        public Map<String, String> headerParams;
        public Map<String, String> bodyParams;
        public String fileParamName;
        public File file;
    }

    /**
     * 单个线性上传任务。
     */
     private class SerialUploadTask extends AsyncTask<Void, Void, Boolean> {

        private String mId;
        private UploadParams mParams;
        private XSerialUploadListener mListener;

        public SerialUploadTask(UploadParams param, XSerialUploadListener listener) {
            mParams = param;
            mListener = listener;
            mId = "" + System.currentTimeMillis();
        }

        public String getId() {
            return mId;
        }

        @Override
        protected void onPreExecute() {
            if (mListener != null)
                mListener.beforeUpload(mParams.url);
            mUploadMgr.setUploadListener(mListener);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return mUploadMgr.upload(
                    mParams.url,
                    mParams.headerParams,
                    mParams.bodyParams,
                    mParams.fileParamName,
                    mParams.file);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mUploadMgr.setUploadListener(null);
            if (mListener != null)
                mListener.afterUpload(mParams.url);
            notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mUploadMgr.setUploadListener(null);
            if (mListener != null)
                mListener.afterUpload(mParams.url);
            notifyTaskFinished(this);
        }
    }
}
