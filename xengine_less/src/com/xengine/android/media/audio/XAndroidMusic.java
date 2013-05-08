package com.xengine.android.media.audio;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by 赵之韵.
 * Date: 12-2-27
 * Time: 上午10:00
 */
public class XAndroidMusic implements XMusic{
    private MediaPlayer player;
    private boolean isPrepared = true;

    XAndroidMusic(MediaPlayer player) {
        this.player = player;
    }

    @Override
    public void dispose () {
        if (player == null) return;
        if (player.isPlaying()) player.stop();
        player.release();
        player = null;
    }

    @Override
    public boolean isLooping () {
        return player.isLooping();
    }

    @Override
    public boolean isPlaying () {
        return player.isPlaying();
    }

    @Override
    public void pause () {
        if (player.isPlaying()) player.pause();
    }

    @Override
    public void play () {
        if (player.isPlaying()) return;

        try {
            if (!isPrepared) {
                player.prepare();
                isPrepared = true;
            }
            player.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLooping (boolean isLooping) {
        player.setLooping(isLooping);
    }

    @Override
    public void setVolume (float volume) {
        player.setVolume(volume, volume);
    }

    @Override
    public void stop () {
        if (isPrepared) {
            player.seekTo(0);
        }
        player.stop();
        isPrepared = false;
    }

    public float getPosition () {
        return player.getCurrentPosition() / 1000f;
    }
}
