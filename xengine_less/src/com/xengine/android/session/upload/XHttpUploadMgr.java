package com.xengine.android.session.upload;

import com.xengine.android.session.http.XHttp;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

        HttpPost httpPost = new HttpPost(url);
        // 设置post请求的body
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            // 设置file
            reqEntity.addPart(fileParamName, new FileBody(file));
            // 设置body中的其他参数
            for (Map.Entry<String, String> entry : bodyParams.entrySet())
                reqEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onError(url, "Unsupported Encoding Params");
            return false;
        }
        httpPost.setEntity(reqEntity);
        // 设置post请求的header
        httpPost.addHeader(reqEntity.getContentType());
        for (Map.Entry<String, String> entry : bodyParams.entrySet())
            httpPost.addHeader(entry.getKey(), entry.getValue());

        if (mListener != null)
            mListener.onStart(url);

        // 发送请求，上传
        HttpResponse httpResponse = mHttpClient.execute(httpPost, false);

        if (mListener != null)
            mListener.onComplete(url, httpResponse);

        return true;
    }

    @Override
    public void setUploadListener(XUploadListener listener) {
        mListener = listener;
    }
}
