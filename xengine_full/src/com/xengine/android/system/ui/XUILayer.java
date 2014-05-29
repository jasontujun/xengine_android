package com.xengine.android.system.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * 界面中的图层
 * Created by 赵之韵.
 * Date: 11-11-14
 * Time: 上午8:37
 * Email: ttxzmorln@163.com
 */
public interface XUILayer extends XUIFrameStateListener, XUILayerStateListener, XUIHandler,
        XResourceSupport, XViewSupport, XBackable, XMenuable, XMobileSupport {

    /**
     * 设置层的id，通常情况下用于设置两个层之间的位置关系
     */
    void setId(int id);

    /**
     * 获取层的id
     */
    int getId();

    /**
     * 设置图层进入界面的时候的动画
     */
    void setInAnimation(Animation in);

    /**
     * 设置图层退出界面的时候的动画
     */
    void setOutAnimation(Animation out);

    /**
     * 获取图层的进入动画
     */
    Animation getInAnimation();

    /**
     * 获取图层的退出动画
     */
    Animation getOutAnimation();

    /**
     * 清楚图层的进入动画和退出动画
     */
    void clearAnimation();

    /**
     * 返回装载本图层的内容的相对布局
     */
    RelativeLayout getContent();

    /**
     * 获取图层所在窗口
     */
    XUIFrame getUIFrame();

    /**
     * 设置图层的内容
     */
    void setContentView(int layout);

    /**
     * 设置图层的内容
     */
    void setContentView(RelativeLayout layout);

    /**
     * 获取Context
     */
    Context getContext();

    /**
     * 在框架中显示本图层
     */
    void show();

    /**
     * 在框架中隐藏本图层
     */
    void hide();

    /**
     * 返回使用过的图片列表
     */
    List<String> getUsedBitmaps();

    /**
     * 设置整个图层的背景图
     * @param background 背景图的图片名称
     */
    void setBackground(String background);

    /**
     * 向图层中添加view
     */
    void addView(View view, RelativeLayout.LayoutParams lp);

    /**
     * 向图层中添加组件
     * @param component 需要添加的组件
     * @param lp 组件的布局参数
     */
    void addComponent(XUIComponent component, RelativeLayout.LayoutParams lp);

    /**
     * 删除图层中的组件
     */
    void removeComponent(XUIComponent component);

    /**
     * 由窗口调用，通知图层窗口刚刚创建完成
     */
    void notifyFrameCreated();

    /**
     * 由窗口调用，通知图层窗口显示了
     */
    void notifyFrameDisplay();

    /**
     * 由窗口调用，通知图层窗口将要隐藏
     */
    void notifyFrameInvisible();

    /**
     * 由窗口调用，通知图层窗口将要退出。
     */
    void notifyFrameExit();

    /**
     * 由窗口调用，通知图层，被添加到窗口当中了
     */
    void notifyLayerAddedToFrame();

    /**
     * 由窗口调用，通知图层，被其他图层覆盖了
     */
    void notifyLayerCovered();

    /**
     * 由窗口调用，通知图层，图层上面覆盖的图层已经退出，图层又重新显示出来
     */
    void notifyLayerUnCovered();

    /**
     * 由窗口调用，通知图层，被从窗口中删除了
     */
    void notifyLayerRemovedFromFrame();

    /**
     * 退出本图层
     */
    void exit();
}
