package com.xengine.android.full.system.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

/**
 * View工具类，方便在系统中创建、查找view，为view设置前景和背景图片。
 * Created by 赵之韵.
 * Date: 12-3-1
 * Time: 上午7:15
 */
public interface XViewSupport {
    /**
     * 获取当前系统中的context
     */
    Context getContext();
    
    /**
     * 根据id查找相应的view
     */
    View findViewById(int id);

    /**
     * 根据id查找相应的view同时设置view的背景图片
     * @param id view的id
     * @param background 背景图片的文件名
     * @return id对应的View
     */
    View findViewById(int id, String background);

    /**
     * 根据id查找相应的ImageView同时设置ImageView的前景图片
     * @param id view的id
     * @param picName 图片名
     * @return id对应的view
     */
    ImageView findImageViewById(int id, String picName);

    /**
     * 设置ImageView的前景图片
     * @param view 需要设置前景的ImageView
     * @param picPath 图片相对于assets文件夹的路径
     */
    void setImageViewPic(ImageView view, String picPath);

    /**
     * 设置View的背景图片
     * @param view 需要设置背景的view
     * @param background 背景图片相对于assets文件夹的路径
     */
    void setViewBackground(View view, String background);

    /**
     * 设置9patch图片
     * @param view
     * @param bgName
     */
    void setScalableBackground(View view, String bgName);
}
