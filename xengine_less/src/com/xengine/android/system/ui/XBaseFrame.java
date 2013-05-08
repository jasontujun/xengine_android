package com.xengine.android.system.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xengine.android.media.audio.XAndroidAudio;
import com.xengine.android.media.audio.XAudio;
import com.xengine.android.media.audio.XMusic;
import com.xengine.android.media.audio.XSound;
import com.xengine.android.media.graphics.XAndroidGraphics;
import com.xengine.android.media.graphics.XAndroidScreen;
import com.xengine.android.media.graphics.XGraphics;
import com.xengine.android.media.graphics.XScreen;
import com.xengine.android.media.image.XAndroidImageLocalMgr;
import com.xengine.android.system.mobile.XAndroidMobileMgr;
import com.xengine.android.system.mobile.XMobileMgr;
import com.xengine.android.system.ssm.XAndroidSSM;
import com.xengine.android.system.ssm.XSystemStateManager;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Frame基础类……
 */
public abstract class XBaseFrame extends Activity implements XUIFrame {

    private static final String TAG = "UIFrame";

    /**
     * 所有组件的容器根
     */
    private RelativeLayout root;

    /**
     * 图片池
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
     * 系统状态管理器
     */
    private XSystemStateManager ssm;

    /**
     * 窗口中已经添加的图层
     */
    private ArrayList<XUILayer> layers = new ArrayList<XUILayer>();

    /**
     * 窗口当前的状态
     */
    private XUIFrameState frameState;

    /**
     * 在窗口中使用过的图片
     */
    private ArrayList<String> usedPics = new ArrayList<String>();

    /**
     * 即将要删除的图层里面的图片
     */
    private ArrayList<String> tobeRecycled = new ArrayList<String>();

    /**
     * 回收图片
     */
    private static final int RECYCLE_ALL = 0;

    /**
     * 用于异步回收图片的handler
     */
    private Handler recycleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (tobeRecycled.size() > 0) {
                graphics.recycleAll(tobeRecycled);
                tobeRecycled.clear();
            }
        }
    };

    /**
     * 手机第三方功能管理器
     */
    private XMobileMgr mobileMgr;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preInit(this);

        ssm = XAndroidSSM.getInstance();
        setSystemStateManager(ssm);

        if(isFullScreen()) {
            // 去掉标题栏，并全屏
            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        root = new RelativeLayout(this);
        setContentView(root);

        graphics = new XAndroidGraphics(this);

        audio = new XAndroidAudio(this);

        screen = new XAndroidScreen(this);


        XAndroidImageLocalMgr.getInstance().
                init(screen.getScreenWidth(), screen.getScreenHeight());

        // 用户自定义初始化（设置ImgDir）
        init(this);

        // 初始化手机功能管理器
        mobileMgr = new XAndroidMobileMgr(this,
                screen.getScreenWidth(), screen.getScreenHeight());
        mobileMgr.setPhotoDir(XAndroidImageLocalMgr.getInstance().getImgDir());

        onFrameCreated();
        for(XUILayer layer: layers) {
            layer.notifyFrameCreated();
        }

        changeFrameState(XUIFrameState.CREATED);
    }

    @Override
    public void setImageViewPic(ImageView view, String picPath){
        Bitmap pic = graphics().getBitmap(picPath);
        if(pic != null) {
            view.setImageBitmap(pic);
            graphics().increaseReferenceCount(picPath);
        }
    }

    @Override
    public void setViewBackground(View view, String background) {
        Bitmap pic = graphics().getBitmap(background);
        if(pic != null) {
            view.setBackgroundDrawable(new BitmapDrawable(pic));
            graphics().increaseReferenceCount(background);
        }
    }

    @Override
    public void setScalableBackground(View view, String background) {
        Bitmap pic = graphics().getScalableBitmap(background);
        if(pic != null) {
            view.setBackgroundDrawable(graphics().createScalableDrawable(pic));
            graphics().increaseReferenceCount(background);
        }
    }


    @Override
    public void exit() {
        this.finish();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(isBackKeyDisabled()) {
                    return true;
                }else {
                    return super.onKeyDown(keyCode, e);
                }
            case KeyEvent.KEYCODE_MENU:
                if(isKeyMenuDisable()) {
                    return true;
                }else {
                    return super.onKeyDown(keyCode, e);
                }
        }
        return false;
    }

    @Override
    public void addLayer(XUILayer layer) {
        addLayer(layer,
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.FILL_PARENT));
    }

    @Override
    public void addLayer(final XUILayer layer, RelativeLayout.LayoutParams layoutParams) {
        XLog.d(TAG, "Add layer to root.");
        layer.getContent().setVisibility(View.INVISIBLE);
        XUILayer oldLayer = null;
        if(layers.size() == 0) {
            layers.add(layer);
            layer.notifyLayerAddedToFrame();
            root.addView(layer.getContent(), layoutParams);
        }else {
            oldLayer = layers.get(layers.size() - 1);
            layers.add(layer);
            layer.notifyLayerAddedToFrame();
            root.addView(layer.getContent(), layoutParams);
        }
        // 执行进入动画
        Animation in = layer.getInAnimation();
        if(in != null) {
            in.reset();
            final XUILayer oldLayerCopy = oldLayer;
            in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    XLog.d(TAG, "Layer start in animation.");
                    layer.getContent().setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(oldLayerCopy != null) {
                        oldLayerCopy.notifyLayerCovered();
                        root.removeView(oldLayerCopy.getContent());
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            layer.getContent().startAnimation(in);
        }else {
            if(oldLayer != null) {
                oldLayer.notifyLayerCovered();
                root.removeView(oldLayer.getContent());
            }
            layer.getContent().setVisibility(View.VISIBLE);
            XLog.d(TAG, "Layer has no in animation.");
        }
    }

    @Override
    public void removeLayer(final XUILayer layer) {
        if(layers.contains(layer)) {
            Animation out = layer.getOutAnimation();
            if(out != null) {
                out.reset();
                out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        XLog.d(TAG, "Layer start out animation.");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        removeLayerFromRoot(layer);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                layer.getContent().startAnimation(out);
            }else {
                removeLayerFromRoot(layer);
            }
        }
    }

    /**
     * 实际执行删除layer的动作
     */
    private void removeLayerFromRoot(XUILayer layer) {
        XLog.d(TAG, "Remove layer from root.");
        if(layers.size() == 1) {
            layers.remove(layer);
            layer.notifyLayerRemovedFromFrame();
            root.removeView(layer.getContent());
            recycleBitmaps(layer.getUsedBitmaps());
            exit();
        }else {
            layers.remove(layer);
            XUILayer nextDisplayLayer = layers.get(layers.size() - 1);
            nextDisplayLayer.notifyLayerUnCovered();
            // TIP 将之前的图层添加回来。
            root.addView(nextDisplayLayer.getContent(), root.getChildCount() - 2);
            nextDisplayLayer.getContent().setVisibility(View.VISIBLE);
            layer.notifyLayerRemovedFromFrame();
            root.removeView(layer.getContent());
            recycleBitmaps(layer.getUsedBitmaps());
        }
    }

    /**
     * 异步回收图片
     */
    private void recycleBitmaps(List<String> bitmaps) {
        this.tobeRecycled.clear();
        this.tobeRecycled.addAll(bitmaps);
        recycleHandler.sendEmptyMessage(RECYCLE_ALL);
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

    /**
     * 设置系统状态管理器
     */
    @Override
    public void setSystemStateManager(XSystemStateManager manager) {
        this.ssm = manager;
    }

    /**
     * 返回系统状态管理器
     */
    public XSystemStateManager getSystemStateManager() {
        return ssm;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        changeFrameState(XUIFrameState.RESTARTED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        for(XUILayer layer: layers) {
            layer.notifyFrameDisplay();
        }
        onFrameDisplay();
        changeFrameState(XUIFrameState.STARTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        audio.resume();
        changeFrameState(XUIFrameState.RESUMED);
    }

    @Override
    protected void onPause() {
        audio.pause();
        super.onPause();
        changeFrameState(XUIFrameState.PAUSED);
    }

    @Override
    protected void onStop() {
        for(XUILayer layer: layers) {
            layer.notifyFrameInvisible();
        }
        onFrameInvisible();
        super.onStop();
        changeFrameState(XUIFrameState.STOPPED);
    }

    @Override
    protected void onDestroy() {
        for(XUILayer layer: layers) {
            layer.notifyFrameExit();
        }
        onFrameExit();
        graphics.forceRecycleAll();
        audio.dispose();
        super.onDestroy();
        changeFrameState(XUIFrameState.DESTROYED);
    }

    /**
     * 改变窗口的状态
     */
    private void changeFrameState(XUIFrameState newState) {
        frameState = newState;
        ssm.notifyUIStateChanged(this, newState);
    }

    @Override
    public XUIFrameState getFrameState() {
        return frameState;
    }

    /**
     * 判断窗口是否处在可见周期内
     */
    private boolean inVisibleState() {
        return XUIFrameState.inVisibleState(frameState);
    }

    @Override
    public XGraphics graphics() {
        return graphics;
    }

    @Override
    public XAudio audio() {
        return audio;
    }

    @Override
    public XScreen screen() {
        return screen;
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
    public int back(){
        return XBackType.NOTHING_TO_BACK;
    }

    @Override
    public XUILayer getTopLayer() {
        if(layers.size() == 0) {
            return null;
        }
        return layers.get(layers.size() - 1);
    }

    @Override
    public XUILayer getSecondTopLayer() {
        if(layers.size() > 1) {
            return layers.get(layers.size() - 2);
        }
        return null;
    }

    @Override
    public XMobileMgr mobileMgr() {
        return mobileMgr;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mobileMgr.onInvokeResult(getContext(), requestCode, resultCode, data);// TIP 别漏了
    }
}
