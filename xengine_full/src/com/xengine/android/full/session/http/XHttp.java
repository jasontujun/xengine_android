package com.xengine.android.full.session.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface XHttp {

    HttpResponse execute(HttpUriRequest req, boolean isZipped);

    void abort();

    boolean isDisposed();

    void dispose();

    void registerProgressListener(XHttpProgressListener listener);

    void unregisterProgressListener(XHttpProgressListener listener);

    void addHttpResponseFilter(XHttpResponseFilter filter);

    void removeHttpResponseFilter(XHttpResponseFilter filter);

    boolean isNetworkAvailable();

    void setCookie(String name, String value);

    int getConnectionTimeOut();

    void setConnectionTimeOut(int connectionTimeOut);

    int getSoTimeOut();

    void setSoTimeOut(int soTimeOut);
}
