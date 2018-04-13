package com.xgw.custommediaplayer.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.socks.library.KLog;

/**
 * Created by XieGuangwei on 2018/4/13.
 */

public class VideoCoverUtils {
    public static void load(Context context,ImageView imageView,String coverUrl) {
        KLog.e("load--->" + coverUrl);
        Glide.with(context)
                .load(coverUrl)
                .into(imageView);
        KLog.e("current thread:" + Thread.currentThread().getName());
    }


}
