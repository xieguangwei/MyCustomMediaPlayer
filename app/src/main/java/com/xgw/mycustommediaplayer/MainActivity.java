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
        List<VideoBean> videoBeen = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            VideoBean videoBean = new VideoBean();
            videoBean.setName("视频" + i);
            videoBean.setUrl("http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super");
            videoBean.setPlaying(i == 0);
            videoBeen.add(videoBean);
        }
        mediaPlayer.setVideoList(videoBeen);
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
                params.height = SizeUtils.dp2px(300);
                mediaPlayer.setLayoutParams(params);
                mediaPlayer.setScreenParams(ScreenStatus.SCREEN_STATUS_NORMAL);
            }
        });
        mediaPlayer.setController(this, new MyVideoControlManager());
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
