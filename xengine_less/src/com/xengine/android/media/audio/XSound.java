package com.xengine.android.media.audio;

/**
 * Created by 赵之韵.
 * Date: 12-2-27
 * Time: 上午10:04
 */
public interface XSound {
    /**
     * 播放音效
     */
    public void play();

    /**
     * 播放音效
     * @param volume 音效的音量，音量的范围是[0,1]
     */
    public void play(float volume);

    /**
     * 停止播放音效
     */
    public void stop();

    /**
     * 回收音效资源
     */
    public void dispose();
}
