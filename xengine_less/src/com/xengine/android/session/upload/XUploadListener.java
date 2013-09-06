package com.xengine.android.session.upload;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-7-31
 * Time: 下午6:17
 * To change this template use File | Settings | File Templates.
 */
public interface XUploadListener {

    void onStart(String url);

    void onComplete(String url, int statusCode);

    void onError(String url, String errorStr);
}
