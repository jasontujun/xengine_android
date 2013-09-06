package com.xengine.android.session.http.java;

import android.content.Context;
import com.xengine.android.session.http.*;
import com.xengine.android.utils.XLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * XJavaHttpClient是实现XHttp接口的实现类。
 * 本质是基于HttpURLConnection的包装类，
 * 添加了Cookie的管理，通信过程监听。
 * @see java.net.HttpURLConnection
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-3
 * Time: 下午2:38
 * To change this template use File | Settings | File Templates.
 */
public class XJavaHttpClient implements XHttp {

    private static final String TAG = XJavaHttpClient.class.getSimpleName();

    private Context mContext;
    private HttpURLConnection mCurrentRequest;
    private List<XHttpProgressListener> mProgressListeners;
    private boolean mIsDisposed;// 标识是否通信线程池状态，是否已经关闭
    private int mConnectionTimeOut;// 尝试建立连接的等待时间，默认为10秒。
    private int mResponseTimeOut;// 等待数据返回时间，默认为10秒。

    public XJavaHttpClient(Context context) {
        mContext = context;
        mIsDisposed = false;
        mConnectionTimeOut = 10 * 1000;// 默认为10秒
        mResponseTimeOut = 10 * 1000; // 默认为10秒
        mProgressListeners = new ArrayList<XHttpProgressListener>();
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

        if (!XNetworkUtil.isNetworkAvailable(mContext)) {
            XLog.d(TAG, "network not available.");
            for(XHttpProgressListener listener: mProgressListeners) {
                listener.onNetworkBroken();
            }
            return null;
        }

        // 构造HttpRequest
        XJavaHttpRequest javaRequest = (XJavaHttpRequest) req;
        javaRequest.setTimeOut(mConnectionTimeOut, mResponseTimeOut);
        HttpURLConnection request = javaRequest.toJavaHttpRequest();
        mCurrentRequest = request;

        for (XHttpProgressListener listener: mProgressListeners)
            listener.onSendRequest(req);

        try {
            // 和服务器通信
            XLog.d(TAG, "Execute request to " + request.getURL());
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                // 构造HttpResponse
                XJavaHttpResponse javaResponse = new XJavaHttpResponse();
                javaResponse.setConnection(request);
                javaResponse.setStatusCode(request.getResponseCode());
                javaResponse.setContent(inputStream);
                javaResponse.setContentLength(request.getContentLength());
                javaResponse.setAllHeaders(request.getHeaderFields());
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
//            mCurrentRequest.disconnect();// 不能在此disconnect，会关闭InputStream
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
    public boolean isDisposed() {
        return mIsDisposed;
    }

    @Override
    public void dispose() {
        if (!mIsDisposed) {
            abort();
            mContext = null;
            mIsDisposed = true;
        }
    }

    @Override
    public void setCookie(String name, String value) {
        // TODO 实现Cookie的保存
    }

    @Override
    public void clearCookie() {
        // TODO 实现Cookie的清除
    }

    @Override
    public int getConnectionTimeOut() {
        return mConnectionTimeOut;
    }

    @Override
    public void setConnectionTimeOut(int connectionTimeOut) {
        mConnectionTimeOut = connectionTimeOut;
    }

    @Override
    public int getResponseTimeOut() {
        return mResponseTimeOut;
    }

    @Override
    public void setResponseTimeOut(int responseTimeOut) {
        mResponseTimeOut = responseTimeOut;
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
