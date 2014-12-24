package com.xengine.android.session.http.java;

import android.content.Context;
import android.text.TextUtils;
import com.xengine.android.session.http.*;
import com.xengine.android.utils.XApnUtil;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XNetworkUtil;
import org.apache.http.HttpHost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MIME;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * XJavaHttpClient是实现XHttp接口的实现类。
 * 本质是基于HttpURLConnection的包装类，
 * 添加了Cookie的管理。（目前只支持Set-Cookie，不支持Set-Cookie2）
 * 添加了对通信过程的监听。
 * @see java.net.HttpURLConnection
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-3
 * Time: 下午2:38
 */
public class XJavaHttpClient extends XBaseHttp {

    private static final String TAG = XJavaHttpClient.class.getSimpleName();

    private HttpURLConnection mCurrentRequest;

    public XJavaHttpClient(Context context, String userAgent) {
        super(context, userAgent);
    }

    @Override
    public XHttpRequest newRequest(String url) {
        return new XJavaHttpRequest().setUrl(url);
    }

    @Override
    public XHttpResponse execute(XHttpRequest req) {
        if (req == null || !(req instanceof XJavaHttpRequest))
            throw new IllegalArgumentException("XHttpRequest is not correct. Needed XJavaHttpRequest!");

        if (mIsDisposed)
            throw new IllegalStateException("HttpClient has been disposed!");

        if (!XNetworkUtil.isNetworkConnected(mContext)) {
            XLog.d(TAG, "network not available.");
            for (XHttpProgressListener listener: mProgressListeners)
                listener.onNetworkBroken();
            return null;
        }

        // 构造HttpRequest
        XJavaHttpRequest javaRequest = (XJavaHttpRequest) req;
        javaRequest.setTimeOut(mConnectionTimeOut, mResponseTimeOut);
        javaRequest.setUserAgent(mUserAgent);
        javaRequest.setCookies(mCookieStore.getCookies());// 设置cookie
        // 判断是否WAP类型的APN连接，决定是否需要设置APN代理
        Proxy proxy = null;
        XApnUtil.Apn[] apn = XApnUtil.getCurrentApn(mContext);
        if (apn != null && apn.length > 0) {
            if (XApnUtil.Apn.CTWAP.equals(apn[0])) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(XApnUtil.CT_PROXY, XApnUtil.CM_UNI_CT_PORT));
            } else if (XApnUtil.Apn.CMWAP.equals(apn[0])
                    || XApnUtil.Apn.UNIWAP.equals(apn[0])
                    || XApnUtil.Apn._3GWAP.equals(apn[0])) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(XApnUtil.CM_UNI_PROXY, XApnUtil.CM_UNI_CT_PORT));
            }
        }
        // 创建实际的连接
        HttpURLConnection request = javaRequest.toJavaHttpRequest(proxy);
        mCurrentRequest = request;

        for (XHttpProgressListener listener: mProgressListeners)
            listener.onSendRequest(req);

        try {
            // 和服务器通信
            XLog.d(TAG, "Execute request to " + request.getURL());
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                // 获取Response中的Cookie，并存入CookieStore
                List<String> cookies = request.getHeaderFields().get("Set-Cookie");
                if (cookies != null) {
                    for (int i = 0; i < cookies.size(); i++) {
                        Cookie cookie = XJavaHttpUtil.createCookie(cookies.get(i));
                        if (cookie != null)
                            mCookieStore.addCookie(cookie);
                    }
                }
                // 设置Response的Charset
                Charset charset = null;
                List<String> contentTypeStrs = request.getHeaderFields().get(MIME.CONTENT_TYPE);
                if (contentTypeStrs != null && contentTypeStrs.size() > 0)
                    charset = XJavaHttpUtil.getResponseCharset(contentTypeStrs.get(0));

                // 构造HttpResponse
                XJavaHttpResponse javaResponse = new XJavaHttpResponse();
                javaResponse.setConnection(request);
                javaResponse.setStatusCode(request.getResponseCode());
                javaResponse.setContentLength(request.getContentLength());
                javaResponse.setContentType(charset);
                javaResponse.setAllHeaders(request.getHeaderFields());
                // 如果response是压缩的，则自动用GZIPInputStream转换一下
                String contentEncoding = request.getHeaderField(HTTP.CONTENT_ENCODING);
                if (!TextUtils.isEmpty(contentEncoding) && contentEncoding.equalsIgnoreCase("gzip")) {
                    javaResponse.setContent(new GZIPInputStream((inputStream)));
                } else {
                    javaResponse.setContent(inputStream);
                }
                for (XHttpProgressListener listener: mProgressListeners)
                    listener.onReceiveResponse(javaResponse);

                XLog.d(TAG, "return response, statusCode:"
                        + javaResponse.getStatusCode()
                        + ",contentLength:" + javaResponse.getContentLength());
                return javaResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
            XLog.d(TAG, "http connection error.");
            for (XHttpProgressListener listener: mProgressListeners)
                listener.onException(e);
        } finally {
//            mCurrentRequest.disconnect();// TIP 不能在此disconnect，会关闭InputStream
            mCurrentRequest = null;
        }
        return null;
    }

    @Override
    public void abort() {
        if (mCurrentRequest != null) {
            mCurrentRequest.disconnect();
            mCurrentRequest = null;
        }
    }

    @Override
    public void dispose() {
        if (!mIsDisposed) {
            abort();
            mContext = null;
            mIsDisposed = true;
            clearCookie();
        }
    }
}
