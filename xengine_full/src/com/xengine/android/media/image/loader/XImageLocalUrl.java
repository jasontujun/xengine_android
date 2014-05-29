package com.xengine.android.media.image.loader;

/**
 * 本地图片地址的特殊值，用于标记图片加载过程中的一些特殊状态。
 * Created by jasontujun.
 * Date: 12-10-25
 * Time: 下午10:59
 */
public class XImageLocalUrl {

    public static final String IMG_LOADING = "loading";// 图片正在下载

    public static final String IMG_ERROR = "error";// 图片加载错误

    public static final String IMG_DEFAULT = "error";// 默认图片

    public static final String IMG_EMPTY = "error";// 占位图片
}
