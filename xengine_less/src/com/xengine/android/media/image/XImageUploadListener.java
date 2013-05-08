package com.xengine.android.media.image;

/**
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午5:06
 */
public interface XImageUploadListener {

    void onBeforeUpload(String id);

    void onFinishUpload(String id, int result);

    int doUpload(String id);
}
