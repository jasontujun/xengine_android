package com.xengine.android.session.http.apache;

import android.content.Context;
import android.text.TextUtils;
import com.xengine.android.session.http.*;
import com.xengine.android.utils.XLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XApacheHttpClient是实现XHttp接口的实现类。
 * 一个XApacheHttpClient对象就是一个通信线程池，
 * 本质是基于DefaultHttpClient的包装类。
 * @see org.apache.http.impl.client.DefaultHttpClient
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-2
 * Time: 下午3:38
 * To change this template use File | Settings | File Templates.
 */
public class XApacheHttpClient implements XHttp {

    private static final String TAG = XApacheHttpClient.class.getSimpleName();
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String GZIP = "gzip";

    private Context mContext;
    private HttpContext mHttpContext;
    private CookieStore mCookieStore;
    private DefaultHttpClient mHttpClient;
    private HttpUriRequest mCurrentRequest;
    private List<XHttpProgressListener> mProgressListeners;
    private XHttpTransferListener mTransferListener;
    private boolean mIsDisposed;// 标识是否通信线程池状态，是否已经关闭
    private int mConnectionTimeOut;// 尝试建立连接的等待时间，默认为10秒。
    private int mResponseTimeOut;// 等待数据返回时间，默认为10秒。

    public XApacheHttpClient(Context context, String userAgent) {
        mContext = context;
        mIsDisposed = false;
        mConnectionTimeOut = 10 * 1000;// 默认为10秒
        mResponseTimeOut = 10 * 1000; // 默认为10秒
        mProgressListeners = new ArrayList<XHttpProgressListener>();

        mCookieStore = new BasicCookieStore();
        mHttpContext = new BasicHttpContext();
        mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
        HttpParams params = new BasicHttpParams();
        if (!TextUtils.isEmpty(userAgent))
            HttpProtocolParams.setUserAgent(params, userAgent);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setConnectionTimeout(params, mConnectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, mConnectionTimeOut);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, false);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);// 线程池
        mHttpClient = new DefaultHttpClient(cm, params);
    }

    /**
     * 获取用于XApacheHttpClient的请求对象
     * @return
     */
    @Override
    public XHttpRequest newRequest(String url) {
        XApacheHttpRequest request = new XApacheHttpRequest();
        request.setUrl(url);
        request.setListener(mTransferListener);
        return request;
    }

    @Override
    public XHttpResponse execute(XHttpRequest req) {
        return execute(req, false);
    }

    public void setTransferListener(XHttpTransferListener transferListener) {
        mTransferListener = transferListener;
    }

    public XHttpResponse execute(XHttpRequest req, boolean isZipped) {
        if (req == null || !(req instanceof XApacheHttpRequest))
            throw new IllegalArgumentException("XHttpRequest is not correct. Needed XApacheHttpRequest!");

        if (mIsDisposed)
            throw new IllegalStateException("HttpClient has been disposed!");

        if (!XNetworkUtil.isNetworkAvailable(mContext)) {
            XLog.d(TAG, "network not available.");
            for(XHttpProgressListener listener: mProgressListeners) {
                listener.onNetworkBroken();
            }
            return null;
        }

        // 构造HttpUriRequest
        XApacheHttpRequest apacheRequest = (XApacheHttpRequest) req;
        HttpUriRequest request = apacheRequest.toApacheHttpRequest();
        mCurrentRequest = request;
        if (isZipped) {
            request.addHeader(ACCEPT_ENCODING, GZIP);
            request.addHeader(HTTP.CONTENT_ENCODING, GZIP);
        }

        for (XHttpProgressListener listener: mProgressListeners)
            listener.onSendRequest(req);

        try {
            // 和服务器通信
            XLog.d(TAG, "Execute request to " + apacheRequest.getUrl());
            HttpResponse response = mHttpClient.execute(request, mHttpContext);

            if (response != null) {
                // 构造apacheResponse
                XApacheHttpResponse apacheResponse = new XApacheHttpResponse();
                if (response.getStatusLine() != null)
                    apacheResponse.setStatusCode(response.getStatusLine().getStatusCode());
                if (response.getEntity() != null) {
                    apacheResponse.setContent(response.getEntity().getContent());
                    apacheResponse.setContentLength(response.getEntity().getContentLength());
                }
                if (response.getAllHeaders() != null) {
                    Header[] headers = response.getAllHeaders();
                    Map<String, List<String>> wrapperHeaders = new HashMap<String, List<String>>();
                    for (Header header : headers) {
                        if (wrapperHeaders.containsKey(header.getName())) {
                            List<String> valueList = wrapperHeaders.get(header.getName());
                            valueList.add(header.getValue());
                        } else {
                            List<String> valueList = new ArrayList<String>();
                            valueList.add(header.getValue());
                            wrapperHeaders.put(header.getName(), valueList);
                        }
                    }
                    apacheResponse.setAllHeaders(wrapperHeaders);
                }
                for (XHttpProgressListener listener: mProgressListeners)
                    listener.onReceiveResponse(apacheResponse);

                return apacheResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
            XLog.d(TAG, "http connection error.");
            for (XHttpProgressListener listener: mProgressListeners)
                listener.onException(e);
        } finally {
            mCurrentRequest = null;
        }
        return null;
    }

    @Override
    public void setCookie(String name, String value) {
        List<Cookie> cookies = mCookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i ++ ) {
            if (cookies.get(i).equals(name)) {
                cookies.remove(i);
                break;
            }
        }
        mCookieStore.addCookie(new BasicClientCookie(name, value));
    }

    @Override
    public void clearCookie() {
        mCookieStore.clear();
    }

    @Override
    public int getConnectionTimeOut() {
        return mConnectionTimeOut;
    }

    @Override
    public void setConnectionTimeOut(int connectionTimeOut) {
        this.mConnectionTimeOut = connectionTimeOut;
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        mHttpClient.setParams(params);
    }

    @Override
    public int getResponseTimeOut() {
        return mResponseTimeOut;
    }

    @Override
    public void setResponseTimeOut(int responseTimeOut) {
        this.mResponseTimeOut = responseTimeOut;
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setSoTimeout(params, responseTimeOut);
        mHttpClient.setParams(params);
    }

    @Override
    public void abort() {
        if (mCurrentRequest != null)
            mCurrentRequest.abort();
    }

    @Override
    public boolean isDisposed() {
        return mIsDisposed;
    }

    @Override
    public void dispose() {
        if (!mIsDisposed) {
            mContext = null;
            mHttpClient.getConnectionManager().shutdown();
            mIsDisposed = true;
        }
    }

    @Override
    public void registerProgressListener(XHttpProgressListener listener) {
        if (listener != null)
            mProgressListeners.add(listener);
    }

    @Override
    public void unregisterProgressListener(XHttpProgressListener listener) {
        if (listener != null)
            mProgressListeners.remove(listener);
    }
}
