package com.xengine.android.full.session.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xengine.android.full.utils.XLog;
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
import java.util.List;

public class XAndroidHttpClient implements XHttp {

    private static final String TAG = "morln.http";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String GZIP = "gzip";
    public static final String USER_AGENT = "MorlnAndroidHttpClient";

    private ArrayList<XHttpProgressListener> progressListeners
            = new ArrayList<XHttpProgressListener>();
    private ArrayList<XHttpResponseFilter> responseFilters
            = new ArrayList<XHttpResponseFilter>();
    private DefaultHttpClient httpClient;
    private boolean isDisposed = false;
    private Context context;
    private CookieStore cookieStore;
    private HttpContext httpContext;

    /**
     * 尝试建立连接的等待时间，默认为10秒。
     */
    private int connectionTimeOut = 10*1000;

    /**
     * 等待数据返回时间，默认为10秒。
     */
    private int soTimeOut = 10*1000;


    public XAndroidHttpClient(Context context) {
        this.context = context;
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUserAgent(params, USER_AGENT);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, connectionTimeOut);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpClientParams.setRedirecting(params, false);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);
    }

    private HttpUriRequest currentRequest;

    @Override
    public HttpResponse execute(HttpUriRequest req, boolean isZipped) {

        if(isDisposed) {
            throw new IllegalStateException("HttpClient has been disposed!");
        }
        if(!isNetworkAvailable()) {
            XLog.d(TAG, "network not gelivable.");
            for(XHttpProgressListener listener: progressListeners) {
                listener.onNetworkBroken();
            }
            return null;
        }
        currentRequest = req;
        if(isZipped) {
            req.addHeader(ACCEPT_ENCODING, GZIP);
            req.addHeader(HTTP.CONTENT_ENCODING, GZIP);
        }
        HttpResponse response = null;
        try {
            for(XHttpProgressListener listener: progressListeners) {
                listener.onSendRequest(req);
            }
            XLog.d(TAG, "Execute request to " + req.getURI());
            response = httpClient.execute(req, httpContext);
            if(response != null) {
                boolean handled = false;
                for(XHttpResponseFilter filter: responseFilters) {
                    if(filter.handleResponse(response)) {
                        handled = true;
                        break;
                    }
                }
                if(handled) {
                    return null;
                }else {
                    for(XHttpProgressListener listener: progressListeners) {
                        listener.onReceiveResponse(response);
                    }
                }
            }
        } catch (IOException e) {
            XLog.d(TAG, "http connection error.");
            e.printStackTrace();
            for(XHttpProgressListener listener: progressListeners) {
                listener.onException(e);
            }
        }
        currentRequest = null;
        return response;

    }

    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()) {
            XLog.d(TAG, "Network available.");
            return true;
        }else {
            XLog.d(TAG, "Network broken.");
            return false;
        }
    }

    @Override
    public void setCookie(String name, String value) {
        List<Cookie> cookies = cookieStore.getCookies();
        for(int i = 0; i < cookies.size(); i ++ ) {
            if(cookies.get(i).equals(name)) {
                cookies.remove(i);
                break;
            }
        }
        cookieStore.addCookie(new BasicClientCookie(name, value));
    }

    @Override
    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    @Override
    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        httpClient.setParams(params);
    }

    @Override
    public int getSoTimeOut() {
        return soTimeOut;
    }

    @Override
    public void setSoTimeOut(int soTimeOut) {
        this.soTimeOut = soTimeOut;
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setSoTimeout(params, soTimeOut);
        httpClient.setParams(params);
    }

    @Override
    public void abort() {
        if(currentRequest != null) {
            currentRequest.abort();
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if(!isDisposed) {
            context = null;
            httpClient.getConnectionManager().shutdown();
            isDisposed = true;
        }
    }

    @Override
    public void registerProgressListener(XHttpProgressListener listener) {
        if(listener != null) {
            progressListeners.add(listener);
        }
    }

    @Override
    public void unregisterProgressListener(XHttpProgressListener listener) {
        if(listener != null) {
            progressListeners.remove(listener);
        }
    }

    @Override
    public void addHttpResponseFilter(XHttpResponseFilter filter) {
        if(filter != null) {
            responseFilters.add(filter);
        }
    }

    @Override
    public void removeHttpResponseFilter(XHttpResponseFilter filter) {
        if(filter != null) {
            responseFilters.remove(filter);
        }
    }
}
