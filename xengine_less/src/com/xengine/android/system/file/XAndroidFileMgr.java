package com.xengine.android.system.file;

import android.os.Environment;
import com.xengine.android.utils.XLog;

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

    private static XAndroidFileMgr instance;

    public static synchronized XAndroidFileMgr getInstance() {
        if(instance == null) {
            instance = new XAndroidFileMgr();
        }
        return instance;
    }

    private XAndroidFileMgr(){
        subDirs = new HashMap<Integer, File>();
    }

    private String rootName;
    private Map<Integer, File> subDirs;

    @Override
    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    @Override
    public boolean setDir(int type, String dirName, boolean clear) {
        if (type < 0) // 小于0的类型忽略
            return false;

        if (subDirs.containsKey(type) && 0 <= type && type <= 10) // 重复设置保留文件夹
            return false;

        File subDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + rootName + File.separator  + dirName);
        subDirs.put(type, subDir);

        if (!subDir.exists())
            return subDir.mkdirs();
        else if (clear)
            clearDir(type);

        return true;
    }

    @Override
    public File getDir(int type) {
        return subDirs.get(type);
    }

    @Override
    public void clearDir(int type) {
        File subDir = getDir(type);
        if (subDir == null || !subDir.exists())
            return;

        File[] files = subDir.listFiles();
        for (int i = 0; i <files.length; i++)
            files[i].delete();

        XLog.d(TAG, "删除文件夹" + subDir.getName() + "下的文件成功.共计" + files.length + "个文件.");
    }
}
