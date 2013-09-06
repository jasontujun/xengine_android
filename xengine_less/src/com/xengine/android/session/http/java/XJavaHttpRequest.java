package com.xengine.android.session.http.java;

import android.text.TextUtils;
import com.xengine.android.session.http.XBaseHttpRequest;
import com.xengine.android.system.file.XFileUtil;
import org.apache.http.entity.mime.MIME;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-3
 * Time: 下午6:13
 * To change this template use File | Settings | File Templates.
 */
class XJavaHttpRequest extends XBaseHttpRequest {

    protected XJavaHttpRequest() {
        super();
    }

    private int mConnectionTimeOut;
    private int mResponseTimeOut;

    public void setTimeOut(int connectionTimeOut, int responseTimeOut) {
        mConnectionTimeOut = connectionTimeOut;
        mResponseTimeOut = responseTimeOut;
    }

    public HttpURLConnection toJavaHttpRequest() {
        if (TextUtils.isEmpty(getUrl()))
            return null;

        HttpURLConnection request = null;
        switch (getMethod()) {
            case GET:
                request = createGetStyleRequest("GET");
                break;
            case POST:
                request = createPostStyleRequest("POST");
                break;
            case PUT:
                request = createPostStyleRequest("PUT");
                break;
            case DELETE:
                request = createGetStyleRequest("DELTE");
                break;
        }
        return request;
    }

    private HttpURLConnection createGetStyleRequest(String method) {
        try {
            URL requestUrl = new URL(getUrl());
            HttpURLConnection request = (HttpURLConnection) requestUrl.openConnection();
            request.setRequestMethod(method);
            request.setDoOutput(false);
            request.setDoInput(true);
            request.setUseCaches(false);
            request.setConnectTimeout(mConnectionTimeOut);
            request.setReadTimeout(mResponseTimeOut);
            // 设置头部header
            if (getHeaders() != null) {
                for (Map.Entry<String, String> header : getHeaders().entrySet())
                    request.addRequestProperty(header.getKey(), header.getValue());
            }
            return request;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpURLConnection createPostStyleRequest(String method) {
        try {
            URL requestUrl = new URL(getUrl());
            HttpURLConnection request = (HttpURLConnection) requestUrl.openConnection();
            request.setRequestMethod(method);
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setUseCaches(false);
            request.setConnectTimeout(mConnectionTimeOut);
            request.setReadTimeout(mResponseTimeOut);
            // 设置头部header
            if (getHeaders() != null) {
                for (Map.Entry<String, String> header : getHeaders().entrySet())
                    request.addRequestProperty(header.getKey(), header.getValue());
            }
            // 设置ContentType
            String boundary = XJavaHttpUtil.generateBoundary();
            String contentType = XJavaHttpUtil.generateContentType(boundary, getCharset());
            request.setRequestProperty(MIME.CONTENT_TYPE, contentType);
            // 设置内容entity
            OutputStream out = request.getOutputStream();
            if (getFileParams() != null)
                writeFileParams(out, boundary);
            if (getStringParams() != null)
                writeStringParams(out, boundary);
            if (getFileParams() != null && getStringParams() != null)
                paramsEnd(out, boundary);
            out.flush();
            out.close();

            return request;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //普通字符串数据
    private void writeStringParams(OutputStream out, String boundary) {
        try {
            for (Map.Entry<String, String> entry : getStringParams().entrySet()) {
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.TWO_DASHES, out);
                XJavaHttpUtil.writeBytes(boundary, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(MIME.CONTENT_DISPOSITION, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.FIELD_SEP, out);
                XJavaHttpUtil.writeBytes("form-data; name=\"", out);
                XJavaHttpUtil.writeBytes(entry.getKey(), out);
                XJavaHttpUtil.writeBytes("\"", out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(entry.getValue(), out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //文件数据
    private void writeFileParams(OutputStream out, String boundary) {
        try {
            for (Map.Entry<String, File> entry : getFileParams().entrySet()) {
                File file = entry.getValue();
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.TWO_DASHES, out);
                XJavaHttpUtil.writeBytes(boundary, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(MIME.CONTENT_DISPOSITION, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.FIELD_SEP, out);
                XJavaHttpUtil.writeBytes("form-data; name=\"", out);
                XJavaHttpUtil.writeBytes(entry.getKey(), out);
                XJavaHttpUtil.writeBytes("\"; filename=\"", out);
                XJavaHttpUtil.writeBytes(file.getName(), out);
                XJavaHttpUtil.writeBytes("\"", out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(MIME.CONTENT_TYPE, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.FIELD_SEP, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.getContentType(file), out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
                out.write(XFileUtil.file2byte(file));
                XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //添加结尾数据
    private void paramsEnd(OutputStream out, String boundary) {
        try {
            XJavaHttpUtil.writeBytes(XJavaHttpUtil.TWO_DASHES, out);
            XJavaHttpUtil.writeBytes(boundary, out);
            XJavaHttpUtil.writeBytes(XJavaHttpUtil.TWO_DASHES, out);
            XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
            XJavaHttpUtil.writeBytes(XJavaHttpUtil.CR_LF, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
