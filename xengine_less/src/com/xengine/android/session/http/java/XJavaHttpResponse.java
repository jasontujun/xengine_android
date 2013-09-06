package com.xengine.android.session.http.java;

import com.xengine.android.session.http.XBaseHttpResponse;

import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:47
 * To change this template use File | Settings | File Templates.
 */
public class XJavaHttpResponse extends XBaseHttpResponse {

    private HttpURLConnection mConnection;

    protected void setConnection(HttpURLConnection connection) {
        mConnection = connection;
    }

    @Override
    public void consumeContent() {
        super.consumeContent();
        if (mConnection != null)
            mConnection.disconnect();
    }
}
