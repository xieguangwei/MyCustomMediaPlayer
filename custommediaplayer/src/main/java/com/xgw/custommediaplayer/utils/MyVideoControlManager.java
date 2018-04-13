package com.xgw.custommediaplayer.utils;

import android.app.Activity;
import android.app.Service;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.socks.library.KLog;
import com.xgw.custommediaplayer.videogesture.BrightnessHelper;
import com.xgw.custommediaplayer.videogesture.ShowControlLayout;
import com.xgw.custommediaplayer.videogesture.VideoGestureLayout;
import com.xgw.custommediaplayer.videogesture.VideoGestureListener;
import com.xgw.custommediaplayer.view.MyMediaPlayer;

/**
 * Created by XieGuangwei on 2018/1/3.
 * 视频手势管理类
 */

public class MyVideoControlManager implements VideoGestureListener {
    private VideoGestureLayout vgl;
    private ShowControlLayout scl;

    private AudioManager mAudioManager;
    private int maxVolume = 0;
    private int oldVolume = 0;
    private int oldFF_REWProgress = 0;
    private BrightnessHelper mBrightnessHelper;
    private float brightness = 1;
    private Window mWindow;
    private WindowManager.LayoutParams mLayoutParams;
    private float newPlaybackTime;

    private MyMediaPlayer mediaPlayer;

    public void bindView(@NonNull Activity mActivity, @NonNull MyMediaPlayer mediaPlayer, @NonNull VideoGestureLayout vgl, @NonNull ShowControlLayout scl) {
        this.vgl = vgl;
        this.scl = scl;
        this.vgl.setVideoGestureListener(this);

        this.mediaPlayer = mediaPlayer;

        //初始化获取音量属性
        mAudioManager = (AudioManager) mActivity.getSystemService(Service.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //初始化亮度调节
        mBrightnessHelper = new BrightnessHelper(mActivity);

        //下面这是设置当前APP亮度的方法配置
        mWindow = mActivity.getWindow();
        mLayoutParams = mWindow.getAttributes();
        brightness = mLayoutParams.screenBrightness;
    }

    @Override
    public void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!MyMediaPlayManager.getInstance().isPlaying()) {
            return;
        }

        //下面这是设置当前APP亮度的方法
        KLog.d("onBrightnessGesture: old" + brightness);
        float newBrightness = (e1.getY() - e2.getY()) / vgl.getHeight();
        newBrightness += brightness;

        KLog.d("onBrightnessGesture: new" + newBrightness);
        if (newBrightness < 0) {
            newBrightness = 0;
        } else if (newBrightness > 1) {
            newBrightness = 1;
        }
        mLayoutParams.screenBrightness = newBrightness;
        mWindow.setAttributes(mLayoutParams);
        scl.show("亮度", ((int) (newBrightness * 100)) + "%");
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (MyMediaPlayManager.getInstance() == null || !MyMediaPlayManager.getInstance().isPlaying()) {
            return;
        }
        int value = vgl.getHeight() / maxVolume;
        int newVolume = (int) ((e1.getY() - e2.getY()) / value + oldVolume);

        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        } else if (newVolume < 0) {
            newVolume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);


        //要强行转Float类型才能算出小数点，不然结果一直为0
        int volumeProgress = (int) (newVolume / Float.valueOf(maxVolume) * 100);
        scl.show("音量", volumeProgress + "%");
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!MyMediaPlayManager.getInstance().isPlaying() || vgl == null) {
            return;
        }

        float duration = MyMediaPlayManager.getInstance().getDuration()/1000;
        float offset = e2.getX() - e1.getX();
        //根据移动的正负决定快进还是快退
        if (offset > 0) {
//            scl.setImageResource(R.drawable.ff);
            int newFF_REWProgress = (int) (oldFF_REWProgress + offset / vgl.getWidth() * 100);
            if (newFF_REWProgress > 100) {
                newFF_REWProgress = 100;
            }
            newPlaybackTime = (newFF_REWProgress * duration) / 100;
            String newPlaybackTimeStr = String.format("%02d:%02d", ((int) newPlaybackTime / 60), ((int) newPlaybackTime % 60));
            String durationStr = String.format("%02d:%02d", ((int) duration / 60), ((int) duration % 60));
            scl.show("前进", newPlaybackTimeStr + "/" + durationStr);

        } else {
            int newFF_REWProgress = (int) (oldFF_REWProgress + offset / vgl.getWidth() * 100);
            if (newFF_REWProgress < 0) {
                newFF_REWProgress = 0;
            }
            newPlaybackTime = (newFF_REWProgress * duration) / 100;
            String newPlaybackTimeStr = String.format("%02d:%02d", ((int) newPlaybackTime / 60), ((int) newPlaybackTime % 60));
            String durationStr = String.format("%02d:%02d", ((int) duration / 1000), ((int) duration % 60));
            scl.show("后退", newPlaybackTimeStr + "/" + durationStr);
        }
    }

    @Override
    public void onSingleTapGesture(MotionEvent e) {
        KLog.e("single tap");
        if (mediaPlayer == null) {
            return;
        }
        if (!mediaPlayer.isControlVisibility()) {
            mediaPlayer.setControlVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE);
        }
    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        KLog.d("onDoubleTapGesture: ");
    }

    @Override
    public void onDown(MotionEvent e) {
        if (!MyMediaPlayManager.getInstance().isPlaying() || vgl == null) {
            return;
        }

        float duration = MyMediaPlayManager.getInstance().getDuration();
        float currentPlayBackTime = MyMediaPlayManager.getInstance().getCurrentPosition();
        if (duration != 0) {
            oldFF_REWProgress = (int) ((100 * currentPlayBackTime) / duration);
        } else {
            oldFF_REWProgress = 0;
        }
        //每次按下的时候更新当前亮度和音量，还有进度
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        brightness = mLayoutParams.screenBrightness;
        if (brightness == -1) {
            //一开始是默认亮度的时候，获取系统亮度，计算比例值
            brightness = mBrightnessHelper.getBrightness() / 255f;
        }
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {
        if (MyMediaPlayManager.getInstance() == null || !MyMediaPlayManager.getInstance().isPlaying()) {
            return;
        }
        KLog.e("设置进度为" + newPlaybackTime * 1000);
        MyMediaPlayManager.seekTo((int) newPlaybackTime * 1000);
    }
}
