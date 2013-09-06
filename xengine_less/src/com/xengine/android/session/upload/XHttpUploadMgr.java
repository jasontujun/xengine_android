package com.xengine.android.session.upload;

import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;

import java.io.File;
import java.util.Map;

/**
 * 利用http方式进行文件上传的管理类。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 */
public class XHttpUploadMgr implements XUpload {

    private XHttp mHttpClient;
    private XUploadListener mListener;

    public XHttpUploadMgr(XHttp httpClient) {
        this.mHttpClient = httpClient;
    }

    @Override
    public boolean upload(String url, Map<String, String> headerParams,
                          Map<String, String> bodyParams, String fileParamName, File file) {
        if (file == null || !file.exists())
            if (mListener != null)
                mListener.onError(url, "File not exist");

        XHttpRequest request = mHttpClient.newRequest(url)
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addFileParam(fileParamName, file);
        // 设置body中的其他参数
        for (Map.Entry<String, String> entry : bodyParams.entrySet())
            request.addStringParam(entry.getKey(), entry.getValue());
        for (Map.Entry<String, String> entry : bodyParams.entrySet())
            request.addHeader(entry.getKey(), entry.getValue());

        if (mListener != null)
            mListener.onStart(url);

        // 发送请求，上传
        XHttpResponse response = mHttpClient.execute(request);
        response.consumeContent();

        if (mListener != null)
            mListener.onComplete(url, response.getStatusCode());

        return true;
    }

    @Override
    public void setUploadListener(XUploadListener listener) {
        mListener = listener;
    }
}
