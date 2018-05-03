package com.xgw.mycustommediaplayer;

import android.os.Environment;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.socks.library.KLog;
import com.xgw.custommediaplayer.constants.ScreenStatus;
import com.xgw.custommediaplayer.entity.VideoBean;
import com.xgw.custommediaplayer.listener.SimpleVideoPlayListener;
import com.xgw.custommediaplayer.utils.MyVideoControlManager;
import com.xgw.custommediaplayer.utils.VideoCoverUtils;
import com.xgw.custommediaplayer.view.MyMediaPlayer;
import com.xgw.mybaselib.base.BaseActivity;
import com.xgw.mybaselib.utils.SizeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.video_view)
    MyMediaPlayer mediaPlayer;

    @BindView(R.id.extra_iv)
    ImageView extraIv;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mediaPlayer.setVideoList(getVideoListData());
        mediaPlayer.setVideoPlayListener(new SimpleVideoPlayListener() {
            @Override
            public void onCompletion() {
                super.onCompletion();
                KLog.e("播放完成");
            }

            @Override
            public void onPrepared(String url) {
                super.onPrepared(url);
                KLog.e("准备播放");
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                KLog.e("播放出错：" + e.getMessage());
            }

            @Override
            public void onStartFullScreen() {
                super.onStartFullScreen();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mediaPlayer.getLayoutParams();
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                mediaPlayer.setLayoutParams(params);
                mediaPlayer.setScreenParams(ScreenStatus.SCREEN_STATUS_FULL);
            }

            @Override
            public void onExitFullScreen() {
                super.onExitFullScreen();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mediaPlayer.getLayoutParams();
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                params.height = SizeUtils.dp2px(220);
                mediaPlayer.setLayoutParams(params);
                mediaPlayer.setScreenParams(ScreenStatus.SCREEN_STATUS_NORMAL);
            }

            @Override
            public void onCacheProgress(String url, int percentAvailable) {
                super.onCacheProgress(url, percentAvailable);
                KLog.e("cacheurl---" + url + ",percent:" + percentAvailable);
            }
        });
        mediaPlayer.setController(this, new MyVideoControlManager());
    }

    private static List<VideoBean> getVideoListData() {
        List<VideoBean> videoList = new ArrayList<>();
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg",
                "办公室小野开番外了，居然在办公室开澡堂！老板还点赞？",true));
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg",
                "小野在办公室用丝袜做茶叶蛋 边上班边看《外科风云》",false));
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-03_12-52-08.jpg",
                "花盆叫花鸡，怀念玩泥巴，过家家，捡根竹竿当打狗棒的小时候",false));
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-28_18-18-22.jpg",
                "针织方便面，这可能是史上最不方便的方便面",false));
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-26_10-00-28.jpg",
                "小野的下午茶，办公室不只有KPI，也有诗和远方",false));
        videoList.add(new VideoBean(
                "http://p79di6ot0.bkt.clouddn.com/upload/1/video/jghdpdfc_598xvnbv6zjn5ae29473a8eed.mp4",
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/04/2017-04-21_16-37-16.jpg",
                "可乐爆米花，嘭嘭嘭......收花的人说要把我娶回家",false));
        return videoList;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.onPause();
    }
}
