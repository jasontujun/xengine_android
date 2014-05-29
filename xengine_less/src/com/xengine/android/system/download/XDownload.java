package com.xengine.android.system.download;

import java.io.File;

/**
 * 文件下载管理类
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-7-31
 * Time: 下午5:21
 * To change this template use File | Settings | File Templates.
 */
public interface XDownload {

    /**
     * 访问url执行下载并以文件形式保存到本地，返回图片的本地文件名。
     * @param url url地址
     * @param localFile 本地文件文件名。若为空，则保存为远程url地址的名称
     * @return
     */
    boolean download(String url, File localFile);

    /**
     * 访问url执行下载并以文件形式保存到本地，返回图片的本地文件名。
     * @param url url地址
     * @param path 本地路径(不以"/"结尾)
     * @param fileName 文件名。若为空，则保存为远程url地址的名称
     * @return
     */
    boolean download(String url, String path, String fileName);

    /**
     * 设置下载的监听
     * @param listener
     */
    void setDownloadListener(XDownloadListener listener);

}
