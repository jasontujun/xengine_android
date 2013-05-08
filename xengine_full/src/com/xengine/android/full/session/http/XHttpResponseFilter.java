package com.xengine.android.full.session.http;

import org.apache.http.HttpResponse;

public interface XHttpResponseFilter {
    boolean handleResponse(HttpResponse response);
}
