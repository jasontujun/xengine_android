package com.xengine.android.session.http;

import android.content.Context;
import android.text.TextUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-9-11
 * Time: 下午7:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class XBaseHttp implements XHttp{

    protected Context mContext;
    protected String mUserAgent;// 客户端名称
    protected CookieStore mCookieStore;
    protected List<XHttpProgressListener> mProgressListeners;
    protected boolean mIsDisposed;// 标识是否通信线程池状态，是否已经关闭
    protected int mConnectionTimeOut;// 尝试建立连接的等待时间，默认为10秒。
    protected int mResponseTimeOut;// 等待数据返回时间，默认为10秒。


    public XBaseHttp(Context context, String userAgent) {
        mContext = context;
        mUserAgent = userAgent;
        mIsDisposed = false;
        mConnectionTimeOut = 10 * 1000;// 默认为10秒
        mResponseTimeOut = 10 * 1000; // 默认为10秒
        mProgressListeners = new ArrayList<XHttpProgressListener>();
        mCookieStore = new BasicCookieStore();
    }


    @Override
    public boolean isDisposed() {
        return mIsDisposed;
    }


    @Override
    public void addCookie(Cookie cookie) {
        if (cookie == null || TextUtils.isEmpty(cookie.getName()))
            return;

        mCookieStore.addCookie(cookie);
    }

    @Override
    public void setCookie(Cookie cookie) {
        if (cookie == null || TextUtils.isEmpty(cookie.getName()))
            return;

        List<Cookie> cookies = mCookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i ++ ) {
            if (cookies.get(i).equals(cookie.getName())) {
                cookies.remove(i);
                break;
            }
        }
        mCookieStore.addCookie(cookie);
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
