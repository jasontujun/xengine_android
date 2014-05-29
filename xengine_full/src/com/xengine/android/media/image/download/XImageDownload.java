package com.xengine.android.media.image.download;

import android.graphics.Bitmap;
import com.xengine.android.system.download.XDownload;

/**
 * 图片下载接口
 * Created by jasontujun.
 * Date: 12-10-30
 * Time: 下午4:08
 */
public interface XImageDownload extends XDownload {

    public static final String FORMAT_PNG = "png";
    public static final String FORMAT_JPG = "jpg";
    public static final String FORMAT_JPEG = "jpeg";
    public static final String FORMAT_GIF = "gif";
    public static final String DEFAULT_FORMAT = FORMAT_JPG;

    /**
     * 把图片下载并保存到本地(指定格式和压缩率)，返回图片的本地文件名。
     * compress默认为XImageLocalMgr.DEFAULT_COMPRESS.
     * sWidth为屏幕宽，sHeight默认为屏幕高。
     * @param imgUrl 图片url地址
     * @param format 格式
     * @return
     */
    String downloadImg2File(String imgUrl, String format);

    /**
     * 把图片下载并保存到本地(指定格式和压缩率)，返回图片的本地文件名。
     * sWidth为屏幕宽，sHeight默认为屏幕高。
     * @param imgUrl 图片url地址
     * @param format 格式
     * @param compress 压缩率
     * @return
     */
    String downloadImg2File(String imgUrl, String format, int compress);

    /**
     * 把图片下载并保存到本地，返回图片的本地文件名。
     * @param imgUrl 图片url地址
     * @param format 格式
     * @param compress 压缩率
     * @param sWidth 显示宽度；如果小于等于0，则使用ScreenWidth
     * @param sHeight 显示高度；如果小于等于0，则使用ScreenHeight
     * @return 存到本地的文件名
     * @see com.xengine.android.media.image.processor.XImageProcessor
     */
    String downloadImg2File(String imgUrl, String format,
                            int compress, int sWidth, int sHeight);


    /**
     * 访问网络，把图片下载，在内存中处理(根据屏幕宽度和高度来压缩)，并返回bitmap。
     * @param imgUrl 图片url地址
     * @param format 格式
     * @return Bitmap对象
     */
    Bitmap downloadImg2Bmp(String imgUrl, String format);

    /**
     * 把图片下载，在内存中处理(根据调用者指定的宽度和高度来压缩)，并返回bitmap。
     * @param imgUrl 图片url地址
     * @param format 格式
     * @param sWidth 显示宽度；如果小于等于0，则使用ScreenWidth
     * @param sHeight 显示高度；如果小于等于0，则使用ScreenHeight
     * @return Bitmap对象
     * @see com.xengine.android.media.image.processor.XImageProcessor
     */
    Bitmap downloadImg2Bmp(String imgUrl, String format, int sWidth, int sHeight);
}
