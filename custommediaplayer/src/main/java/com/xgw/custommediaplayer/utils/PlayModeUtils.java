package com.xgw.custommediaplayer.utils;


import com.xgw.custommediaplayer.constants.PlayMode;
import com.xgw.custommediaplayer.constants.VideoSpKey;

/**
 * Created by XieGuangwei on 2018/4/11.
 */

public class PlayModeUtils {
    /**
     * 获取当前播放模式
     *
     * @return
     */
    public static int getPlayMode() {
        return SPUtils.getInstance(VideoSpKey.PLAY_MODE_SP_NAME).getInt(VideoSpKey.PLAY_MODE_SP_KEY, PlayMode.PLAY_MODE_LIST_ORDER);
    }

    /**
     * 设置播放模式
     *
     * @param playMode
     */
    public static int setPlayMode(int playMode) {
        if (playMode != PlayMode.PLAY_MODE_LIST_CYCLE && playMode != PlayMode.PLAY_MODE_LIST_ORDER && playMode != PlayMode.PLAY_MODE_SINGLE_CYCLE) {
            playMode = PlayMode.PLAY_MODE_LIST_ORDER;
        }
        SPUtils.getInstance(VideoSpKey.PLAY_MODE_SP_NAME).put(VideoSpKey.PLAY_MODE_SP_KEY, playMode);
        return playMode;
    }
}
