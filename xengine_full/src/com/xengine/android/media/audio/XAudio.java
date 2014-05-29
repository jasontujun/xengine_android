package com.xengine.android.media.audio;

/**
 * Created by 赵之韵.
 * Date: 12-2-29
 * Time: 上午12:32
 */
public interface XAudio {

    XMusic newMusic(String path);
    
    XSound newSound(String path);

    void dispose();

    void pause();

    void resume();
}
