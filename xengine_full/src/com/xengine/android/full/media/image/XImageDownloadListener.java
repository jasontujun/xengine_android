package com.xengine.android.full.media.image;

/**
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午5:06
 */
public interface XImageDownloadListener {

    void onBeforeDownload(String id);

    void onFinishDownload(String id, String result);
}
