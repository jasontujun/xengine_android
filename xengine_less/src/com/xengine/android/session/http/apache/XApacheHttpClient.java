package com.xengine.android.session.http.apache;

import android.content.Context;
import android.text.TextUtils;
import com.xengine.android.session.http.*;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XNetworkUtil;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
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
public class XApacheHttpClient extends XBaseHttp {

    private static final String TAG = XApacheHttpClient.class.getSimpleName();
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String GZIP = "gzip";

    public final static int MAX_TOTAL_CONNECTIONS = 800;// 最大连接数
    public final static int MAX_ROUTE_CONNECTIONS = 400;// 每个路由最大连接数

    private HttpContext mHttpContext;
    private DefaultHttpClient mHttpClient;
    private HttpUriRequest mCurrentRequest;
    private XHttpTransferListener mTransferListener;

    public XApacheHttpClient(Context context, String userAgent) {
        super(context, userAgent);

        mHttpContext = new BasicHttpContext();
        mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
        HttpParams params = new BasicHttpParams();
        // http协议参数设置
        if (!TextUtils.isEmpty(userAgent))
            HttpProtocolParams.setUserAgent(params, userAgent);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(params, true);
        // http连接池设置
        ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);// 设置最大连接数
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);// 设置每个路由最大连接数
        // http连接参数设置
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setConnectionTimeout(params, mConnectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, mConnectionTimeOut);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setTcpNoDelay(params, true);
        // http重定向设置
        HttpClientParams.setRedirecting(params, false);
        // scheme设置
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        // 线程池设置
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        mHttpClient = new DefaultHttpClient(cm, params);
    }

    @Override
    public XHttpRequest newRequest(String url) {
        XApacheHttpRequest request = new XApacheHttpRequest();
        request.setUrl(url);
        request.setListener(mTransferListener);
        return request;
    }

    /**
     * 用XHttpRequest生成HttpUriRequest请求
     * @param req
     * @param isZipped
     * @return
     */
    private HttpUriRequest prepareHttpRequest(XHttpRequest req, boolean isZipped) {
        if (mIsDisposed)
            return null;

        if (!XNetworkUtil.isNetworkConnected(mContext)) {
            XLog.d(TAG, "network not connected.");
            for (XHttpProgressListener listener: mProgressListeners)
                listener.onNetworkBroken();
            return null;
        }

        // 构造HttpUriRequest
        XApacheHttpRequest apacheRequest = (XApacheHttpRequest) req;
        HttpUriRequest request = (HttpUriRequest) apacheRequest.toApacheHttpRequest();
        if (isZipped) {
            request.addHeader(ACCEPT_ENCODING, GZIP);
            request.addHeader(HTTP.CONTENT_ENCODING, GZIP);
        }
        return request;
    }

    @Override
    public XHttpResponse execute(XHttpRequest req) {
        return execute(req, false);
    }

    public XHttpResponse execute(XHttpRequest req, boolean isZipped) {
        if (req == null || !(req instanceof XApacheHttpRequest))
            throw new IllegalArgumentException("XHttpRequest is not correct. Required XApacheHttpRequest!");

        try {
            // 构造HttpUriRequest
            XApacheHttpRequest apacheRequest = (XApacheHttpRequest) req;
            HttpUriRequest request = prepareHttpRequest(apacheRequest, isZipped);
            mCurrentRequest = request;
            if (request == null)
                return null;
            for (XHttpProgressListener listener: mProgressListeners)
                listener.onSendRequest(req);

            // 和服务器通信
            XLog.d(TAG, "Execute request to " + apacheRequest.getUrl());
            HttpResponse response = mHttpClient.execute(request, mHttpContext);

            // 自动处理重定向
            if (isRedirect(request, response)) {
                XLog.d(TAG, "Request need redirect: " + apacheRequest.getUrl());
                response = handleRedirect(req, response, isZipped);
            }

            if (response == null)
                return  null;
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

    public void setTransferListener(XHttpTransferListener transferListener) {
        mTransferListener = transferListener;
    }

    @Override
    public void setConnectionTimeOut(int connectionTimeOut) {
        super.setConnectionTimeOut(connectionTimeOut);
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        mHttpClient.setParams(params);
    }

    @Override
    public void setResponseTimeOut(int responseTimeOut) {
        super.setResponseTimeOut(responseTimeOut);
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
    public void dispose() {
        if (!mIsDisposed) {
            mIsDisposed = true;
            mContext = null;
            mHttpClient.getConnectionManager().shutdown();
            clearCookie();
        }
    }

    private boolean isRedirect(HttpRequest request, HttpResponse response) {
        if (response == null || response.getStatusLine() == null)
            return false;

        int statusCode = response.getStatusLine().getStatusCode();
        XLog.d(TAG, "IsRedirect? statusCode: " + statusCode);
        switch (statusCode) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                String method = request.getRequestLine().getMethod();
                return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpHead.METHOD_NAME);
            case HttpStatus.SC_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    private HttpResponse handleRedirect(XHttpRequest req, HttpResponse response,
                                        boolean isZipped) {
        while (true) {
            if (response == null)
                return null;

            // 302重定向，获取新的url
            Header header = response.getFirstHeader("Location");
            if (header == null) {
                XLog.d(TAG, "Location field is null!");
                return response;
            }
            String redirectUrl = header.getValue();
            XLog.d(TAG, "redirectionUrl:" + redirectUrl);
            req.setUrl(redirectUrl);

            // 关闭上一次请求
            if (response.getEntity() != null)
                try {
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            try {
                // 生成新的url的请求
                HttpUriRequest request = prepareHttpRequest(req, isZipped);
                mCurrentRequest = request;
                if (request == null)
                    return null;
                // 发起新的通信
                response = mHttpClient.execute(request, mHttpContext);
                // 如果还是302重定向，则继续重新发起请求，否则跳出循环
                if (!isRedirect(request, response))
                    break;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return response;
    }
}
