package com.xengine.android.system.upload;

import com.xengine.android.system.upload.XUploadListener;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午5:23
 * To change this template use File | Settings | File Templates.
 */
public interface XSerialUploadListener extends XUploadListener {

    /**
     * 开始upload之前。在UI线程中。
     */
    void beforeUpload(String url);

    /**
     * upload结束之后。在UI线程中。
     */
    void afterUpload(String url);
}
