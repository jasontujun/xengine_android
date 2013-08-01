package com.xengine.android.session.download;

import com.xengine.android.session.http.XHttp;
import com.xengine.android.utils.XStringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

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
public class XHttpDownloadMgr implements XDownloadMgr {

    private XHttp mHttpClient;
    private XDownloadListener mListener;

    public XHttpDownloadMgr(XHttp httpClient) {
        this.mHttpClient = httpClient;
    }

    @Override
    public boolean download(String url, String path, String fileName) {
        if (mListener != null)
            mListener.onStart(url);

        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = mHttpClient.execute(httpGet, false);

        if (response == null) {
            if (mListener != null)
                mListener.onError(url, "No Response");
            return false;
        }

        try {
            InputStream is = response.getEntity().getContent();
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
                    mListener.doDownload(url, downloadPosition);
            }
            is.close();
            if (mListener != null)
                mListener.onComplete(url, localFileName);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onError(url, "File Not Found");
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null)
                mListener.onError(url, "IO Exception");
        }
        return false;
    }


    public void setDownloadListener(XDownloadListener listener) {
        mListener = listener;
    }
}
