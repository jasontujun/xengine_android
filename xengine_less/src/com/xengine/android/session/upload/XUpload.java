package com.xengine.android.session.upload;

import java.io.File;
import java.util.Map;

/**
 * 文件上传管理类
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-1
 * Time: 上午10:36
 * To change this template use File | Settings | File Templates.
 */
public interface XUpload {

    /**
     * 上传文件
     * @param url 上传的远程url地址
     * @param headerParams 请求的header参数
     * @param bodyParams 请求的body参数
     * @param fileParamName 请求的头部参数
     * @param file 文件
     * @return
     */
    boolean upload(String url, Map<String, String> headerParams,
                   Map<String, String> bodyParams, String fileParamName, File file);

    /**
     * 设置上传的监听
     * @param listener
     */
    void setUploadListener(XUploadListener listener);
}
