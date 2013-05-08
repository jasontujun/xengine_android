package com.xengine.android.full.session.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Http通信过程监听器
 * Created by 赵之韵.
 * Date: 11-12-29
 * Time: 下午8:46
 */
public interface XHttpProgressListener {

    void onSendRequest(HttpUriRequest request);
    void onReceiveResponse(HttpResponse response);
    void onException(Exception e);
    void onNetworkBroken();

}
