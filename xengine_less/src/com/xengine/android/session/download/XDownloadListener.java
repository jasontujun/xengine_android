package com.xengine.android.session.download;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-7-31
 * Time: 下午6:17
 * To change this template use File | Settings | File Templates.
 */
public interface XDownloadListener {

    void onStart(String url);

    void onDownloading(String url, long position);

    void onComplete(String url, String localFileName);

    void onError(String url, String errorStr);
}
