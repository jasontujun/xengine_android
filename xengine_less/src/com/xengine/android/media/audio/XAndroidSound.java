package com.xengine.android.media.audio;

import android.media.SoundPool;

import java.util.ArrayList;

/**
 * Created by 赵之韵.
 * Date: 12-2-27
 * Time: 上午10:03
 */
public class XAndroidSound implements XSound{
    final SoundPool soundPool;
    final int soundId;
    final ArrayList<Integer> streamIds = new ArrayList<Integer>();

    XAndroidSound(SoundPool pool, int soundId) {
        this.soundPool = pool;
        this.soundId = soundId;
    }

    @Override
    public void dispose () {
        soundPool.unload(soundId);
    }

    @Override
    public void play () {
        play(1);
    }

    @Override
    public void play (float volume) {
        if (streamIds.size() == 8) {
            streamIds.remove(7);
        }
        streamIds.add(soundPool.play(soundId, volume, volume, 1, 0, 1));
    }

    public void stop () {
        for (int i = 0, n = streamIds.size(); i < n; i++)
            soundPool.stop(streamIds.get(i));
    }
}
