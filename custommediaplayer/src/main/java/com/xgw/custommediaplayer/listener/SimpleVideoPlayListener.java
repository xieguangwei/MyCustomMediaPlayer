package com.xgw.custommediaplayer.listener;

/**
 * Created by XieGuangwei on 2018/4/12.
 */

public abstract class SimpleVideoPlayListener implements VideoPlayListener {
    @Override
    public void onCompletion() {

    }

    @Override
    public void onPrepared(String url) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onStartFullScreen() {

    }

    @Override
    public void onExitFullScreen() {

    }

    @Override
    public void onCacheProgress(String url, int percentAvailable) {

    }
}
