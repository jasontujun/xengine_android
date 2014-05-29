package com.xengine.android.system.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

/**
 * 存在于Layer中的组件
 * Created by dbds.
 * Date: 11-11-23
 * Time: 下午2:50
 * Email: ttxz1984@sina.com
 */
public interface XUIComponent extends XUIFrameStateListener, XUIHandler, XUILayerStateListener,
        XResourceSupport, XViewSupport, XBackable, XMobileSupport {

    /**
     * 返回本组件的内容
     */
    View getContent();

    /**
     * 给组件设置一个id
     */
    void setId(int id);

    /**
     * 获取组建的id
     */
    int getId();

    /**
     * 获取父图层
     */
    XUILayer parentLayer();

    /**
     * 设置组件内容
     */
    void setContentView(int layout);

    /**
     * 设置组件内容
     */
    void setContentView(View view);

    /**
     * 设置组件的背景图片
     */
    void setBackground(String background);

    /**
     * 显示本组件
     */
    void show();

    /**
     * 隐藏本组件
     */
    void hide();

    /**
     * 获取Context
     */
    Context getContext();

    /**
     * 在组件上应用一个动画
     */
    void startAnimation(Animation animation);

}
