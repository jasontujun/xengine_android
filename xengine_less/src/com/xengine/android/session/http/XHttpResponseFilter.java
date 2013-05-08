package com.xengine.android.session.http;

import org.apache.http.HttpResponse;

public interface XHttpResponseFilter {
    boolean handleResponse(HttpResponse response);
}
