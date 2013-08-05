package com.xengine.android.system.file;

import android.os.Environment;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.io.*;
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
        mSubDirs = new HashMap<Integer, File>();
    }

    private String mRootName;
    private Map<Integer, File> mSubDirs;

    @Override
    public void setRootName(String rootName) {
        if (XStringUtil.isNullOrEmpty(rootName))
            return;
        if (!XStringUtil.isNullOrEmpty(mRootName) && mRootName.equals(rootName))
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
        if (XStringUtil.isNullOrEmpty(dirName))
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
        if (subDir == null || !subDir.exists())
            return;

        File[] files = subDir.listFiles();
        for (int i = 0; i <files.length; i++)
            files[i].delete();

        XLog.d(TAG, "删除文件夹" + subDir.getName() + "下的文件成功.共计" + files.length + "个文件.");
    }

    @Override
    public boolean copyFile(File oldFile, File newFile) {
        try {
            int byteread = 0;
            if (oldFile.exists()) { // 文件存在时
                InputStream is = new FileInputStream(oldFile); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1444];
                while ((byteread = is.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                is.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public byte[] file2byte(File file) throws IOException {
        if(file == null)
            return null;

        InputStream is = new FileInputStream(file);
        // 判断文件大小
        long length = file.length();
        if (length > Integer.MAX_VALUE) // 文件太大，无法读取
            throw new IOException("File is to large "+file.getName());
        // 创建一个数据来保存文件数据
        byte[] bytes = new byte[(int)length];
        // 读取数据到byte数组中
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length &&
                (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset < bytes.length)
            throw new IOException("Could not completely read file "+file.getName());
        is.close();
        return bytes;
    }

    @Override
    public boolean byte2file(byte[] bytes, File file) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
