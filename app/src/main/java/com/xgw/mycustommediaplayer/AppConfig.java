package com.xgw.mycustommediaplayer;

import android.app.Application;

import com.xgw.custommediaplayer.utils.MyMediaPlayerDelegate;
import com.xgw.mybaselib.utils.Utils;

/**
 * Created by XieGuangwei on 2018/4/12.
 */

public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyMediaPlayerDelegate.getInstance().initApp(this).initLog(true);
        Utils.init(this);
    }
}
