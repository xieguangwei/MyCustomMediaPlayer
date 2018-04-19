package com.xgw.custommediaplayer.listener;

/**
 * Created by XieGuangwei on 2018/3/28.
 */

public interface VideoPlayListener {
    /**
     * 播放完成
     */
    void onCompletion();

    /**
     * 播放准备
     *
     * @param url 当前正在播放的视频的url
     */
    void onPrepared(String url);

    /**
     * 播放出错
     *
     * @param e
     */
    void onError(Exception e);

    /**
     * 打开全屏
     */
    void onStartFullScreen();

    /**
     * 退出全屏
     */
    void onExitFullScreen();

    /**
     * 缓存进度
     * @param url 缓存的url
     * @param percentAvailable 可用进度
     */
    void onCacheProgress(String url,int percentAvailable);
}
