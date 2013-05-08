package com.xengine.android.media.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 赵之韵.
 * Date: 12-2-27
 * Time: 上午10:40
 */
public class XAndroidAudio implements XAudio{
    private static final String TAG = "XAudioManager";
    private SoundPool soundMgr;
    private final AudioManager manager;
    private AssetManager assets;
    private final HashMap<String, XAndroidMusic> musicPool = new HashMap<String, XAndroidMusic>();
    private final HashMap<String, XAndroidSound> soundPool = new HashMap<String, XAndroidSound>();
    private final HashMap<XAndroidMusic, Boolean> playingMask = new HashMap<XAndroidMusic, Boolean>();

    public XAndroidAudio(Context context) {
        soundMgr = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);
        manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        assets = context.getAssets();
    }

    @Override
    public XMusic newMusic (String path) {
        if(musicPool.containsKey(path)) {
            return musicPool.get(path);
        }

        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            AssetFileDescriptor fd = assets.openFd(path);
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.prepare();
            XAndroidMusic music = new XAndroidMusic(mediaPlayer);
            musicPool.put(path, music);
            fd.close();
            return music;
        } catch (Exception e) {
            //XLog.e(TAG, "Can't open music file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public XSound newSound (String path) {
        if(soundPool.containsKey(path)) {
            return soundPool.get(path);
        }

        try {
            AssetFileDescriptor fd = assets.openFd(path);
            XAndroidSound sound = new XAndroidSound(soundMgr, soundMgr.load(fd, 1));
            soundPool.put(path, sound);
            fd.close();
            return sound;
        } catch (Exception e) {
            //XLog.d(TAG, "Can't open sound file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void dispose() {
        synchronized (musicPool) {
            for(Map.Entry<String, XAndroidMusic> entry: musicPool.entrySet()) {
                XAndroidMusic music = entry.getValue();
                music.dispose();
            }
            musicPool.clear();
        }
        soundPool.clear();
        soundMgr.release();
    }

    @Override
    public void pause() {
        playingMask.clear();
        synchronized (musicPool) {
            for(Map.Entry<String, XAndroidMusic> entry: musicPool.entrySet()) {
                XAndroidMusic music = entry.getValue();
                if(music.isPlaying()) {
                    playingMask.put(music, true);
                }else {
                    playingMask.put(music, false);
                }
            }
        }
    }

    @Override
    public void resume() {
        synchronized (musicPool) {
            for(Map.Entry<String, XAndroidMusic> entry: musicPool.entrySet()) {
                Boolean isPlay = playingMask.get(entry.getValue());
                if(isPlay != null && isPlay) {
                    entry.getValue().play();
                }
            }
        }
    }
}
