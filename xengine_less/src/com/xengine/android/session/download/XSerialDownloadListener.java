package com.xengine.android.session.download;

import com.xengine.android.session.download.XDownloadListener;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午5:23
 * To change this template use File | Settings | File Templates.
 */
public interface XSerialDownloadListener extends XDownloadListener {

    /**
     * 开始download之前。在UI线程中。
     */
    void beforeDownload(String url);

    /**
     * download结束之后。在UI线程中。
     */
    void afterDownload(String url);
}
