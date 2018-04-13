package com.xgw.custommediaplayer.videogesture;

import android.view.MotionEvent;

/**
 * Created by XieGuangwei on 2018/1/2.
 * 视频播放手势回调接口
 */

public interface VideoGestureListener {
    /**
     * 亮度手势，手指在Layout左半部上下滑动时候调用
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     */
    void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    /**
     * 音量手势，手指在Layout右半部上下滑动时候调用
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     */
    void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    /**
     * 快进快退手势，手指在Layout左右滑动的时候调用
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     */
    void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    /**
     * 单击手势，确认是单击的时候调用
     *
     * @param e
     */
    void onSingleTapGesture(MotionEvent e);

    /**
     * 双击手势，确认是双击的时候调用
     *
     * @param e
     */
    void onDoubleTapGesture(MotionEvent e);

    /**
     * 按下手势，第一根手指按下时候调用
     *
     * @param e
     */
    void onDown(MotionEvent e);

    /**
     * 快进快退执行后的松开时候调用
     *
     * @param e
     */
    void onEndFF_REW(MotionEvent e);
}
