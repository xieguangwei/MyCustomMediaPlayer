package com.xgw.custommediaplayer.utils;

import android.app.Application;

import com.socks.library.KLog;

/**
 * Created by XieGuangwei on 2018/4/12.
 * 该类用来初始化上下文等相关配置
 */

public class MyMediaPlayerDelegate {
    private static MyMediaPlayerDelegate instance;
    private Application app;
    private int millis;

    public static MyMediaPlayerDelegate getInstance() {
        if (instance == null) {
            synchronized (MyMediaPlayerDelegate.class) {
                instance = new MyMediaPlayerDelegate();
            }
        }
        return instance;
    }

    public MyMediaPlayerDelegate initApp(Application app) {
        this.app = app;
        return this;
    }

    public MyMediaPlayerDelegate initLog(boolean shouldShowLog) {
        KLog.init(shouldShowLog);
        return this;
    }

    public MyMediaPlayerDelegate initControlDismissDelayMillis(int millis) {
        this.millis = millis;
        return this;
    }

    public Application getApp() {
        return app;
    }

    public int getDelayMillis() {
        return this.millis == 0 ? 4000 : this.millis;
    }
}
