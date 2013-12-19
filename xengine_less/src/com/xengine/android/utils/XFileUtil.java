package com.xengine.android.utils;

import java.io.*;

/**
 * 文件工具方法
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午5:45
 */
public class XFileUtil {

    /**
     * 复制文件
     * @param oldFile
     * @param newFile
     * @return
     */
    public static boolean copyFile(File oldFile, File newFile) {
        try {
            int read = 0;
            if (oldFile.exists()) { // 文件存在时
                InputStream is = new FileInputStream(oldFile); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1024];
                while ((read = is.read(buffer)) != -1) {
                    fs.write(buffer, 0, read);
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

    /**
     * 将File转换为byte[]
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static byte[] file2byte(File file) throws IOException {
        if (file == null)
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
        is.close();

        // 确保所有数据均被读取
        if (offset < bytes.length)
            throw new IOException("Could not completely read file "+file.getName());
        return bytes;
    }

    /**
     * 将byte[]转换为File
     * @param bytes
     * @param file
     * @return
     */
    public static boolean byte2file(byte[] bytes, File file) {
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

    /**
     * String转换为File
     * @param res
     * @param file
     */
    public static void string2File(String res, File file) {
        if (file == null)
            return;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedReader = new BufferedReader(new StringReader(res));
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            char buf[] = new char[2 * 1024]; // 字符缓冲区
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (file.exists())
                file.delete();
        }
    }

    /**
     * 清空文件夹。
     * @param dir 文件夹
     * @param removeSelf 是否删除自身
     */
    public static void clearDirectory(File dir, boolean removeSelf) {
        if (dir == null || !dir.exists())
            return;

        File[] files = dir.listFiles();
        if (files != null) // 如果dir不是文件夹，files会为null
            for (int i = 0; i <files.length; i++)
                files[i].delete();

        if (removeSelf)
            dir.delete();
    }
}
