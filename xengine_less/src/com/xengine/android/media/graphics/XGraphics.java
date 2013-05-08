package com.xengine.android.media.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.NinePatchDrawable;

import java.util.List;

/**
 * 图片管理器，为系统提供从assets文件夹载入Bitmap图片的服务。
 * 并根据图片的引用计数，在适当的时候销毁图片。
 * 图片以其在assets文件夹下的路径做为其唯一识别标志。
 * Created by 赵之韵.
 * Date: 12-2-28
 * Time: 上午10:09
 */
public interface XGraphics {
    /**      
     * 根据参数中给定的图片路径，载入图片
     * @param path 图片相对于assets文件夹的路径
     * @return Bitmap对象封装的图片
     */
    Bitmap getBitmap(String path);

    Bitmap getScalableBitmap(String path);

    NinePatchDrawable createScalableDrawable(Bitmap pic);

    /**
     * 增加对某一个图片的引用计数
     * @param path 图片相对于assets文件夹的路径
     */
    void increaseReferenceCount(String path);

    /**
     * 减少对某一个图片的引用计数。
     * TIP 好像这个函数用不到啊。
     * @param path 图片相对于assets文件夹的路径
     */
    void decreaseReferenceCount(String path);

    /**
     * 回收指定图片占用的资源。
     * 对于每一张要回收的图片，系统先查询他的引用计数。
     * 如果引用计数减一以后等于或者小于0，则回收图片。
     * 如果引用计数减一以后大于0，则只将引用计数减一，不回收图片。
     * @param path 图片相对于assets文件夹的路径
     */
    void recyclePic(String path);

    /**
     * 回收所有列表中的图片资源
     * @param paths 需要回收的图片的路径列表
     */
    void recycleAll(List<String> paths);

    /**
     * 强制回收所有的图片资源
     */
    void forceRecycleAll();
}
