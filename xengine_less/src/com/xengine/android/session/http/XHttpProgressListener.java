package com.xengine.android.session.http;

/**
 * Http通信过程监听器
 * Created by 赵之韵.
 * Date: 11-12-29
 * Time: 下午8:46
 */
public interface XHttpProgressListener {

    void onSendRequest(XHttpRequest request);
    void onReceiveResponse(XHttpResponse response);
    void onException(Exception e);
    void onNetworkBroken();

}
