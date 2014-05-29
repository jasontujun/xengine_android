package com.xengine.android.system.file;

import android.os.Environment;
import android.text.TextUtils;
import com.xengine.android.utils.XFileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-5-11
 * Time: 上午11:22
 */
public class XAndroidFileMgr implements XFileMgr {

    private static final String TAG = "FILE";

    private static class SingletonHolder {
        final static XAndroidFileMgr INSTANCE = new XAndroidFileMgr();
    }

    public static XAndroidFileMgr getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private XAndroidFileMgr(){
        mSubDirs = new HashMap<Integer, File>();
    }

    private String mRootName;
    private Map<Integer, File> mSubDirs;

    @Override
    public void setRootName(String rootName) {
        if (TextUtils.isEmpty(rootName))
            return;
        if (!TextUtils.isEmpty(mRootName) && mRootName.equals(rootName))
            return;

        mRootName = rootName;
        File rootDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + mRootName);
        mSubDirs.put(FILE_ROOT, rootDir);
    }

    @Override
    public String getRootName() {
        return mRootName;
    }

    @Override
    public boolean setDir(int type, String dirName, boolean clear) {
        if (type < 0) // 小于0的类型忽略
            return false;
        if (TextUtils.isEmpty(dirName))
            return false;

        if (mSubDirs.containsKey(type) && 0 <= type && type <= 10) // 重复设置保留文件夹
            return false;

        File subDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + mRootName + File.separator  + dirName);
        mSubDirs.put(type, subDir);

        if (!subDir.exists())
            return subDir.mkdirs();
        else if (clear)
            clearDir(type);

        return true;
    }

    @Override
    public File getDir(int type) {
        return mSubDirs.get(type);
    }

    @Override
    public void clearDir(int type) {
        File subDir = getDir(type);
        XFileUtil.clearDirectory(subDir, false);
    }
}
