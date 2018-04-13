package com.xgw.custommediaplayer.utils;

import android.media.MediaPlayer;

/**
 * Created by XieGuangwei on 2018/4/2.
 * MediaPlayer管理类，该类保证整个应用只有一个MediaPlayer对象
 */

public final class MyMediaPlayManager {
    private static MediaPlayer mPlayer;

    private static boolean isFullScreen = false;

    //获取多媒体对象
    public static MediaPlayer getInstance() {
        if (mPlayer == null) {
            synchronized (MyMediaPlayManager.class) {
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                }
            }
        }
        return mPlayer;
    }

    //播放
    public static void play() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    //暂停
    public static void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    //释放
    public static void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 跳转到指定位置
     * @param progress
     */
    public static void seekTo(int progress) {
        if (mPlayer != null) {
            if (progress<=mPlayer.getDuration()){
                mPlayer.seekTo(progress);
            } else {
                mPlayer.seekTo(mPlayer.getDuration());
            }
        }
    }



    public static void setIsFullScreen(boolean isFullScreen) {
        MyMediaPlayManager.isFullScreen = isFullScreen;
    }

    public static boolean isIsFullScreen() {
        return isFullScreen;
    }
}
