package com.xgw.custommediaplayer.videogesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by XieGuangwei on 2018/1/2.
 * 视频手势控制相对布局（视频播放根布局，可手势控制进度、音量、亮度）
 */

public class VideoGestureLayout extends RelativeLayout {
    /**
     * 回调接口
     */
    private VideoGestureListener videoGestureListener;
    /**
     * 手势器
     */
    private GestureDetector mGestureDetector;
    /**
     * 手势器回调接口
     */
    private MySimpleOnGestureListener mySimpleOnGestureListener;

    public VideoGestureLayout(Context context) {
        super(context);
        init(context);
    }

    public VideoGestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoGestureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化方法
     *
     * @param context
     */
    private void init(Context context) {
        mySimpleOnGestureListener = new MySimpleOnGestureListener(this);
        mGestureDetector = new GestureDetector(context,mySimpleOnGestureListener);
        //取消长按，不然会影响滑动
        mGestureDetector.setIsLongpressEnabled(false);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (mySimpleOnGestureListener != null && mySimpleOnGestureListener.isHasFF_REW()) {
                        if (videoGestureListener != null) {
                            videoGestureListener.onEndFF_REW(motionEvent);
                        }
                        mySimpleOnGestureListener.setHasFF_REW(false);
                    }
                }
                //监听触摸事件
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });
    }




    /**
     * 设置监听
     *
     * @param videoGestureListener
     */
    public void setVideoGestureListener(VideoGestureListener videoGestureListener) {
        this.videoGestureListener = videoGestureListener;
        if (mySimpleOnGestureListener != null) {
            mySimpleOnGestureListener.setVideoGestureListener(videoGestureListener);
        }
    }
}
