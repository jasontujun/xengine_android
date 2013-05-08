package com.xengine.android.media.audio;

/**
 * Created by 赵之韵.
 * Date: 12-2-26
 * Time: 下午9:58
 */
public interface XMusic {
    /**
     * 播放音乐。
     * 如果音乐此时是暂停状态，则恢复播放。
     * 如果音乐已经播放完成，则重新播放。
     */
    public void play();

    /**
     * 暂停播放，如果音乐还没有播放或者已经停止，则这次调用会被忽略。
     */
    public void pause();

    /**
     * 停止播放音乐。
     * 下一次调用play()的时候会从头开始播放音乐。
     */
    public void stop();

    /** @return whether this music stream is playing */
    /**
     * 返回音乐是否正在播放状态。
     */
    public boolean isPlaying();

    /**
     * 设置音乐是否循环播放，这个方法可以在任何时间调用。
     * @param isLooping 音乐是否循环播放。
     */
    public void setLooping(boolean isLooping);

    /**
     * 当前音乐是否是循环播放的。
     */
    public boolean isLooping();

    /**
     * 设置音乐播放的音量。
     * @param volume 音乐的音量，取值在[0,1]之间，0无声，1最大声。
     */
    public void setVolume(float volume);

    /**
     * 返回当前音乐播放的位置（单位：毫秒）
     */
    public float getPosition();

    /**
     * 销毁音乐资源
     */
    public void dispose();
}
