package com.xengine.android.session.series;

import android.os.AsyncTask;
import com.xengine.android.session.upload.XUploadListener;
import com.xengine.android.session.upload.XUploadMgr;

import java.io.File;
import java.util.Map;

/**
 * 图片线性上传管理类。
 * Created by 赵之韵.
 * Modified by jasontujun
 * Email: ttxzmorln@163.com
 * Date: 12-3-16
 * Time: 上午9:38
 */
public class XSerialUploadMgr extends
        XBaseSerialMgr<XSerialUploadMgr.UploadParams, XUploadListener> {

    private XUploadMgr mUploadMgr;

    public XSerialUploadMgr(XUploadMgr uploadMgr) {
        super();
        mUploadMgr = uploadMgr;
    }

    @Override
    protected AsyncTask createTask(UploadParams data, XUploadListener listener) {
        return new SerialUploadTask(data, listener);
    }

    @Override
    protected String getTaskId(AsyncTask task) {
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
        private XUploadListener mListener;

        public SerialUploadTask(UploadParams param, XUploadListener listener) {
            mParams = param;
            mListener = listener;
            mId = "" + System.currentTimeMillis();
        }

        public String getId() {
            return mId;
        }

        @Override
        protected void onPreExecute() {
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
            notifyTaskFinished(this);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            notifyTaskFinished(this);
        }
    }
}
