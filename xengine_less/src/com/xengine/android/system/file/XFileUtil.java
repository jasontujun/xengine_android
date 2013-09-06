package com.xengine.android.system.file;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
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

    /**
     * 将File转换为byte[]
     * @param file
     * @return
     * @throws IOException
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
}
