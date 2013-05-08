package com.xengine.android.full.system.mobile;

import java.io.File;

/**
 * Created by jasontujun.
 * Date: 12-4-22
 * Time: 下午10:49
 */
public interface XPhotoListener {

    void onSuccess(File file);

    void onFail();
}
