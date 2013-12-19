package com.xengine.android.session.http.java;

import android.text.TextUtils;
import com.xengine.android.session.http.XBaseHttpRequest;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.utils.XFileUtil;
import com.xengine.android.utils.XLog;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MIME;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-3
 * Time: 下午6:13
 * To change this template use File | Settings | File Templates.
 */
class XJavaHttpRequest extends XBaseHttpRequest {

    private static final String TAG = XJavaHttpRequest.class.getSimpleName();

    protected XJavaHttpRequest() {
        super();
    }

    private int mConnectionTimeOut;
    private int mResponseTimeOut;
    private String mUserAgent;
    private List<Cookie> mCookies;

    public void setTimeOut(int connectionTimeOut, int responseTimeOut) {
        mConnectionTimeOut = connectionTimeOut;
        mResponseTimeOut = responseTimeOut;
    }

    void setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
    }

    public void setCookies(List<Cookie> cookies) {
        mCookies = cookies;
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
                request = createGetStyleRequest("DELETE");
                break;
        }
        return request;
    }

    /**
     * 创建不含Body的请求，包括GET、DELETE类型的请求。
     * @param method
     * @return
     */
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
            // 设置userAgent
            if (!TextUtils.isEmpty(mUserAgent))
                request.addRequestProperty("User-Agent", mUserAgent);
            // 设置cookie
            initCookie(request, getUrl());
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

    /**
     * 创建含有Body的请求，包括POST、PUT类型的请求。
     * @param method
     * @return
     */
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
            // 设置userAgent
            if (!TextUtils.isEmpty(mUserAgent))
                request.addRequestProperty("User-Agent", mUserAgent);
            // 设置cookie
            initCookie(request, getUrl());
            // 设置头部header
            if (getHeaders() != null) {
                for (Map.Entry<String, String> header : getHeaders().entrySet())
                    request.addRequestProperty(header.getKey(), header.getValue());
            }
            // 含有上传文件，Content-Type:multipart/form-data
            if (getFileParams() != null) {
                // 设置ContentType
                String boundary = XJavaHttpUtil.generateBoundary();
                String contentType = XJavaHttpUtil.generateMultiContentType(boundary, getCharset());
                request.setRequestProperty(MIME.CONTENT_TYPE, contentType);
                // 设置内容entity
                OutputStream out = request.getOutputStream();
                writeFileParams(out, boundary);
                if (getStringParams() != null)
                    writeStringParams(out, boundary);
                if (getFileParams() != null && getStringParams() != null)
                    paramsEnd(out, boundary);
                out.flush();
                out.close();
            }
            // 只有字符串参数，Content-Type:application/x-www-form-urlencoded
            else {
                // 设置ContentType
                String contentType = XJavaHttpUtil.generateStringContentType(getCharset());
                request.setRequestProperty(MIME.CONTENT_TYPE, contentType);
                // 设置内容entity
                OutputStream out = request.getOutputStream();
                List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : getStringParams().entrySet())
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                String charset = getCharset() != null ? getCharset() : XHttp.DEF_CONTENT_CHARSET.name();
                String content = URLEncodedUtils.format(parameters, charset);
                out.write(content.getBytes(charset));
                out.flush();
                out.close();
            }

            return request;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 给当前请求设置Cookie字段的值。
     * 多个Cookie信息通过一个Cookie请求头字段回送给WEB服务器。
     * 客户端根据一定匹配规则去筛选符合Cookie,详情参考XJavaUtil.verifyCookie()
     * @see XJavaHttpUtil
     * @param request
     */
    private void initCookie(HttpURLConnection request, String url) {
        if (mCookies == null)
            return;

        XLog.d(TAG, "cookie size:" + mCookies.size() + ",url:" + url);
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : mCookies) {
            if (XJavaHttpUtil.verifyCookie(cookie, url))
                sb.append(cookie.getName())
                        .append("=")
                        .append(cookie.getValue())
                        .append(";");
        }
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        String cookieStr = sb.toString();
        XLog.d(TAG, "Cookie:" + cookieStr);
        request.addRequestProperty("Cookie", cookieStr);
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
