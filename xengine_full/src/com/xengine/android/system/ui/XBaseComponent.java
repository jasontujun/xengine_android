package com.xengine.android.system.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.xengine.android.media.audio.XMusic;
import com.xengine.android.media.audio.XSound;
import com.xengine.android.media.graphics.XScreen;
import com.xengine.android.system.mobile.XMobileMgr;

/**
 * Created by dbds.
 * Date: 11-11-23
 * Time: 下午3:00
 * Email: ttxz1984@sina.com
 */
public abstract class XBaseComponent implements XUIComponent {

    private XUILayer parentLayer;

    private Context context;

    private View content;

    public XBaseComponent(XUILayer parent) {
        this.parentLayer = parent;
        this.context = parent.getContext();
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
    public View getContent() {
        return content;
    }

    @Override
    public Handler getFrameHandler() {
        return parentLayer.getFrameHandler();
    }

    @Override
    public Handler getLayerHandler() {
        return parentLayer.getLayerHandler();
    }

    @Override
    public Context getContext() {
        return context;
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
    public XUILayer parentLayer() {
        return parentLayer;
    }

    @Override
    public void setContentView(int layout) {
        content = View.inflate(context, layout, null);
        // REVISED 防止触摸时间传递到下一层
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void setContentView(View content) {
        this.content = content;
        // REVISED 防止触摸时间传递到下一层
        content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void setBackground(String background) {
        parentLayer.setViewBackground(content, background);
    }

    @Override
    public void setScalableBackground(View view, String background) {
        parentLayer().setScalableBackground(view, background);
    }

    @Override
    public View findViewById(int id) {
        View view = content.findViewById(id);
        return view;
    }

    @Override
    public void setImageViewPic(ImageView view, String picPath) {
        parentLayer.setImageViewPic(view, picPath);
    }

    @Override
    public void setViewBackground(View view, String background) {
        parentLayer.setViewBackground(view, background);
    }

    @Override
    public ImageView findImageViewById(int id, String picPath) {
        ImageView view = (ImageView) content.findViewById(id);
        setImageViewPic(view, picPath);
        return view;
    }

    @Override
    public View findViewById(int id, String background) {
        View view = content.findViewById(id);
        setViewBackground(view, background);
        return view;
    }

    @Override
    public Bitmap getBitmap(String path) {
        return parentLayer.getBitmap(path);
    }

    @Override
    public Bitmap getScalableBitmap(String path) {
        return parentLayer.getScalableBitmap(path);
    }

    @Override
    public XMusic newMusic(String path) {
        return parentLayer.newMusic(path);
    }

    @Override
    public XSound newSound(String path) {
        return parentLayer.newSound(path);
    }

    @Override
    public XScreen screen() {
        return parentLayer.screen();
    }

    @Override
    public void startAnimation(Animation anim) {
        content.startAnimation(anim);
    }

    @Override
    public void show() {
        onFrameDisplay();
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        onFrameInvisible();
        content.setVisibility(View.GONE);
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

    @Override
    public int back(){
        return XBackType.NOTHING_TO_BACK;
    }

    @Override
    public XMobileMgr mobileMgr() {
        return parentLayer.mobileMgr();
    }
}
