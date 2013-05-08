package com.xengine.android.system.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xengine.android.media.audio.XMusic;
import com.xengine.android.media.audio.XSound;
import com.xengine.android.system.ssm.XSystemStateManager;

/**
 * XUIFrame是界面的框架，其中容纳多个图层（XUILayer）。
 * 可以显示图层，隐藏图层。
 * 为界面组件提供容器和基础服务。
 * 通常情况下由一个Activity来实现XUIFrame接口。
 * Created by 赵之韵.
 * Date: 11-11-14
 * Time: 上午8:39
 * Email: ttxzmorln@163.com
 */
public interface XUIFrame extends XUIFrameStateListener, XServiceManager,
        XResourceSupport, XBackable, XMobileSupport {

    /**
     * 获取操作窗口的Handler
     */
    Handler getFrameHandler();

    /**
     * 添加并显示图层
     */
    void addLayer(XUILayer layer);

    /**
     * 添加并显示图层
     * @param layer 要添加的图层
     * @param layoutParams 图层在窗口之中的布局参数
     */
    void addLayer(XUILayer layer, RelativeLayout.LayoutParams layoutParams);

    /**
     * 隐藏图层
     */
    void removeLayer(XUILayer layer);

    /**
     * 返回处在最顶端的图层
     */
    XUILayer getTopLayer();

    /**
     * 返回处在次顶端的图层
     */
    XUILayer getSecondTopLayer();

    /**
     * 窗口是否全屏显示
     */
    boolean isFullScreen();

    /**
     * 是否让窗口的返回键失效
     */
    boolean isBackKeyDisabled();

    /**
     * 是否让menu键失效
     */
    boolean isKeyMenuDisable();

    /**
     * 在系统还没有分配资源的时候进行一些初始化工作。
     */
    void preInit(Context context);

    /**
     * 实现这个函数来完成初始化的工作
     */
    void init(Context context);

    /**
     * 退出该界面
     */
    void exit();

    /**
     * 获取Context
     */
    Context getContext();

    /**
     * 设置系统状态管理器
     */
    void setSystemStateManager(XSystemStateManager manager);

    /**
     * 返回系统状态管理器
     */
    XSystemStateManager getSystemStateManager();

    /**
     * 返回窗口的名称
     */
    String getName();

    /**
     * 返回当前窗口所处的状态
     */
    XUIFrameState getFrameState();

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
     * @param background
     */
    void setScalableBackground(View view, String background);

    /**
     * 获取图片
     */
    Bitmap getBitmap(String path);

    /**
     * 获取一个音乐对象
     */
    XMusic newMusic(String path);

    /**
     * 获取一个音效对象
     */
    XSound newSound(String path);
}
