package com.xengine.android.system.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xengine.android.media.audio.XAudio;
import com.xengine.android.media.audio.XMusic;
import com.xengine.android.media.audio.XSound;
import com.xengine.android.media.graphics.XGraphics;
import com.xengine.android.media.graphics.XScreen;
import com.xengine.android.system.mobile.XMobileMgr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dbds.
 * Date: 11-11-13
 * Time: 下午5:11
 * Email: ttxz1984@sina.com
 */
public abstract class XBaseLayer implements XUILayer {

    /**
     * 退出图层的消息
     *
     */
    private static final int MSG_EXIT = -1;

    /**
     * Context
     */
    private Context context;

    /**
     * 装载组件的父框架
     */
    private XUIFrame uiFrame;

    /**
     * 内容
     */
    private RelativeLayout content;

    /**
     * 图层中添加的组件
     */
    private ArrayList<XUIComponent> components = new ArrayList<XUIComponent>();

    /**
     * 在本图层内使用过的图片
     */
    private ArrayList<String> usedPics = new ArrayList<String>();

    /**
     * 进入动画
     */
    private Animation inAnimation;

    /**
     * 退出动画
     */
    private Animation outAnimation;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EXIT:
                    notifyLayerRemovedFromFrame();
                    getUIFrame().removeLayer(XBaseLayer.this);
                    break;
            }
        }
    };

    /**
     * 图片服务
     */
    private XGraphics graphics;

    /**
     * 声音服务
     */
    private XAudio audio;

    /**
     * 屏幕服务
     */
    private XScreen screen;

    /**
     * 构造函数，记得调用setContentView()哦
     * @param uiFrame
     */
    public XBaseLayer(XUIFrame uiFrame) {
        this.context = uiFrame.getContext();
        this.uiFrame = uiFrame;
        this.graphics = uiFrame.graphics();
        this.audio = uiFrame.audio();
        this.screen = uiFrame.screen();
        setContentView(new RelativeLayout(context));
    }

    @Override
    public Handler getFrameHandler() {
        return uiFrame.getFrameHandler();
    }

    @Override
    public void sendLayerMessage(int msgWhat, Bundle data) {
        Handler handler = getLayerHandler();
        Message msg = handler.obtainMessage();
        msg.what = msgWhat;
        if(data != null) {
            msg.setData(data);
        }
        handler.sendMessage(msg);
    }

    @Override
    public void sendFrameMessage(int msgWhat, Bundle data) {
        Handler handler = getFrameHandler();
        Message msg = handler.obtainMessage();
        msg.what = msgWhat;
        if(data != null) {
            msg.setData(data);
        }
        handler.sendMessage(msg);
    }

    @Override
    public void setId(int id) {
        content.setId(id);
    }

    @Override
    public int getId() {
        return content.getId();
    }

    @Override
    public XUIFrame getUIFrame() {
        return uiFrame;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public RelativeLayout getContent() {
        return content;
    }

    @Override
    public void setContentView(int layout) {
        content = (RelativeLayout) View.inflate(context, layout, null);
        // REVISED 防止触摸时间传递到下一层
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void setContentView(RelativeLayout layout) {
        content = layout;
        // REVISED 防止触摸时间传递到下一层
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public View findViewById(int id) {
        return content.findViewById(id);
    }

    @Override
    public void addView(View view, RelativeLayout.LayoutParams lp) {
        content.addView(view, lp);
    }

    @Override
    public void addComponent(XUIComponent component, RelativeLayout.LayoutParams lp) {
        content.addView(component.getContent(), lp);
        components.add(component);
    }

    @Override
    public void removeComponent(XUIComponent component) {
        content.removeView(component.getContent());
        components.remove(component);
    }

    @Override
    public void setBackground(String background) {
        Bitmap bg = getBitmap(background);
        if(bg != null) {
            content.setBackgroundDrawable(new BitmapDrawable(bg));
        }
    }

    @Override
    public void setScalableBackground(View view, String background) {
        Bitmap bg = getScalableBitmap(background);
        if(bg != null) {
            view.setBackgroundDrawable(getUIFrame().graphics().createScalableDrawable(bg));
        }
    }

    @Override
    public void setViewBackground(View view, String background) {
        Bitmap pic = getBitmap(background);
        if(pic != null) {
            view.setBackgroundDrawable(new BitmapDrawable(pic));
        }
    }

    @Override
    public void setImageViewPic(ImageView view, String picPath) {
        Bitmap pic = getBitmap(picPath);
        if(pic != null) {
            view.setImageBitmap(pic);
        }
    }

    @Override
    public View findViewById(int id, String background) {
        View view = findViewById(id);
        setViewBackground(view, background);
        return view;
    }

    @Override
    public ImageView findImageViewById(int id, String picName) {
        ImageView view = (ImageView) findViewById(id);
        setImageViewPic(view, picName);
        return view;
    }

    @Override
    public Bitmap getBitmap(String path) {
        Bitmap pic = graphics.getBitmap(path);
        if(pic != null) {
            if(!usedPics.contains(path)) {
                usedPics.add(path);
                graphics.increaseReferenceCount(path);
            }
        }
        return pic;
    }

    @Override
    public Bitmap getScalableBitmap(String path){
        Bitmap pic = graphics.getScalableBitmap(path);
        if(pic != null) {
            if(!usedPics.contains(path)) {
                usedPics.add(path);
                graphics.increaseReferenceCount(path);
            }
        }
        return pic;
    }
    
    @Override
    public XMusic newMusic(String path) {
        return audio.newMusic(path);    
    }

    @Override
    public XSound newSound(String path) {
        return audio.newSound(path);
    }

    @Override
    public XScreen screen() {
        return screen;
    }

    @Override
    public List<String> getUsedBitmaps() {
        return new ArrayList<String>(usedPics);
    }

    @Override
    public void show() {
        notifyFrameDisplay();
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        notifyFrameInvisible();
        content.setVisibility(View.GONE);
    }

    @Override
    public void exit() {
        Message msg = handler.obtainMessage();
        msg.what = MSG_EXIT;
        handler.sendMessage(msg);
    }

    @Override
    public void notifyFrameCreated() {
        for(XUIComponent component: components) {
            component.onFrameCreated();
        }
        onFrameCreated();
    }

    @Override
    public void notifyFrameDisplay() {
        for(XUIComponent component: components) {
            component.onFrameDisplay();
        }
        onFrameDisplay();
    }

    @Override
    public void notifyFrameInvisible() {
        for(XUIComponent component: components) {
            component.onFrameInvisible();
        }
        onFrameInvisible();
    }

    @Override
    public void notifyFrameExit() {
        for(XUIComponent component: components) {
            component.onFrameExit();
        }
        onFrameExit();
    }

    @Override
    public void notifyLayerAddedToFrame() {
        for(XUIComponent component: components) {
            component.onLayerAddedToFrame();
        }
        onLayerAddedToFrame();
    }

    @Override
    public void notifyLayerCovered() {
        for(XUIComponent component: components) {
            component.onLayerCovered();
        }
        onLayerCovered();
    }

    @Override
    public void notifyLayerUnCovered() {
        for(XUIComponent component: components) {
            component.onLayerUnCovered();
        }
        onLayerUnCovered();
    }

    @Override
    public void notifyLayerRemovedFromFrame() {
        for(XUIComponent component: components) {
            component.onLayerRemovedFromFrame();
        }
        onLayerRemovedFromFrame();
    }

    @Override
    public void onFrameCreated() {}

    @Override
    public void onFrameDisplay() {}

    @Override
    public void onFrameInvisible() {}

    @Override
    public void onFrameExit() {}

    @Override
    public void onLayerAddedToFrame() {}

    @Override
    public void onLayerCovered() {}

    @Override
    public void onLayerUnCovered() {}

    @Override
    public void onLayerRemovedFromFrame() {}

    /**
     * 图层的进入动画
     */
    @Override
    public Animation getInAnimation() {
        return inAnimation;
    }

    @Override
    public void setInAnimation(Animation inAnimation) {
        this.inAnimation = inAnimation;
    }

    /**
     * 图层的退出动画
     */
    @Override
    public Animation getOutAnimation() {
        return outAnimation;
    }

    @Override
    public void setOutAnimation(Animation outAnimation) {
        this.outAnimation = outAnimation;
    }

    @Override
    public void clearAnimation() {
        content.clearAnimation();
    }

    @Override
     public int back(){
        return XBackType.NOTHING_TO_BACK;
    }

    @Override
    public boolean onMenu() {
        return false;
    }

    @Override
    public XMobileMgr mobileMgr() {
        return uiFrame.mobileMgr();
    }
}
