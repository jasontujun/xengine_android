package com.xengine.android.full.media.image;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片本地存储处理及图片文件管理的接口。
 * Created by jasontujun.
 * Date: 12-10-29
 * Time: 下午10:09
 */
public interface XImageLocalMgr {
    /**
     * 默认图片压缩率
     */
    public static final int DEFAULT_COMPRESS = 75;

    /**
     * 小屏幕高度，用于把图片转换为小图标
     */
    public static final int SMALL_SCREEN_WIDTH = 200;// 单位：pixel
    /**
     * 小屏幕高度，用于把图片转换为小图标
     */
    public static final int SMALL_SCREEN_HEIGHT = 200;// 单位：pixel

    /**
     * 需要的本地缓存空间。单位:MB
     */
    public static final int FREE_SD_CACHE = 5;


    // 图片尺寸类型
    enum ImageSize {
        ORIGIN,// 原始尺寸（加载快）
        SCREEN,// 屏幕尺寸（稳定且保真）
        SMALL// 缩略图尺寸（省空间）
    }

    /**
     * 通过本地文件名，获取文件
     * @param imgName
     * @return
     */
    File getImgFile(String imgName);

    /**
     * 通过本地文件名，根据不同的尺寸类型，获取bitmap
     * 其中包括：
     *      ORIGIN：获取原始图片的bitmap，不进行压缩运算（速度快）
     *      SCREEN：获取适应屏幕大小的bitmap，稳定且高保真
     *      SMALL：获取缩略版的bitmap，占用空间少
     * @param imgName 图片的本地文件名
     * @return
     * @throws java.io.IOException
     */
    Bitmap getLocalImage(String imgName, ImageSize size) throws IOException;

    /**
     * 通过本地文件名，获取Bitmap
     * @param imgName 图片的本地文件名
     * @return
     * @throws java.io.IOException
     */
    Bitmap getLocalImage(String imgName, int sampleWidth, int sampleHeight) throws IOException;

    /**
     * 处理图片(samplesize)，传入byte数组，返回Bitmap
     * @param data 图片字节数组
     * @param sWidth 显示宽度
     * @param sHeight 显示高度
     * @return
     */
    Bitmap processImage2Bmp(byte[] data, int sWidth, int sHeight);

    /**
     * 处理图片(samplesize)，传入两图片输入流，返回Bitmap
     * @param is1 图片输入流（用于计算sampleSize）
     * @param is2 图片输入流（用于保存）
     * @param sWidth 显示宽度
     * @param sHeight 显示高度
     * @param outPadding 需要加载的区域
     * @return 加载后的bitmap。若失败返回null。
     */
    Bitmap processImage2Bmp(InputStream is1, InputStream is2,
                            int sWidth, int sHeight, Rect outPadding);

    /**
     * 处理图片(samplesize, 压缩，存入文件)，
     * 传入byte数组，返回文件名
     * @param data 图片字节数组
     * @param fileName 保存的图片名
     * @param compress 压缩率
     * @param sWidth 显示宽度
     * @param sHeight 显示高度
     * @return
     */
    boolean processImage2File(byte[] data, String fileName,
                              int compress, int sWidth, int sHeight);

    /**
     * 处理图片(samplesize, 压缩，存入文件)，
     * 传入两图片输入流，返回文件名
     * @param is1 图片输入流（用于计算sampleSize）
     * @param is2 图片输入流（用于保存）
     * @param fileName 保存的图片名
     * @param compress 压缩率
     * @param sWidth 显示宽度
     * @param sHeight 显示高度
     * @param outPadding 需要加载的区域
     * @return 保存的文件名
     */
    boolean processImage2File(InputStream is1, InputStream is2, String fileName,
                              int compress, int sWidth, int sHeight, Rect outPadding);

    /**
     * 压缩并保存图片到SD卡上。
     * @param bm
     * @param imgName   图片名（注意后缀名）
     * @param format    格式（PNG | GIF）
     * @param compress
     * @return
     */
    boolean saveImageToSd(String imgName, Bitmap bm,
                          Bitmap.CompressFormat format, int compress);

    /**
     * 保存文件
     * @param imgName 文件名
     * @param inputStream 文件内容
     */
    boolean saveImageToSd(String imgName, InputStream inputStream);

    /**
     * 清空缓存文件夹
     */
    void clearTmpDir();
}
