package com.xengine.android.session.download;

import com.xengine.android.utils.XStringUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-7-31
 * Time: 下午5:59
 * To change this template use File | Settings | File Templates.
 */
public abstract class XBaseDownloadMgr implements XDownloadMgr {

    private XDownloadListener mListener;

    @Override
    public boolean download2File(String url, String path, String fileName) {
        if (mListener != null)
            mListener.onStart(url);

        InputStream is = download(url);
        if (is == null) { // 没有下载流
            if (mListener != null)
                mListener.onError(url, "无法获取文件");
            return false;
        }

        try {
            String localFileName;// 取得文件名，如果输入新文件名，则使用新文件名
            if (XStringUtil.isNullOrEmpty(fileName))
                localFileName = url.substring(url.lastIndexOf("/") + 1);
            else
                localFileName = fileName;

            FileOutputStream FOS = null; // 创建写入文件内存流，
            FOS = new FileOutputStream(path + localFileName);
            byte buf[] = new byte[1024];
            long downloadPosition = 0;
            int numRead;
            while ((numRead = is.read(buf)) != -1) {
                FOS.write(buf, 0, numRead);
                downloadPosition += numRead;
                if (mListener != null)
                    mListener.onDownloading(url, downloadPosition);
            }
            is.close();
            if (mListener != null)
                mListener.onComplete(url, localFileName);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void setDownloadListener(XDownloadListener listener) {
        mListener = listener;
    }
}
