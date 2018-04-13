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
        List<String> paths = StorageUtils.getFilePaths(Environment.getExternalStorageDirectory().getPath());
        List<String> names = StorageUtils.getNames(Environment.getExternalStorageDirectory().getPath());
        final List<VideoBean> videoBeanList = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).endsWith(".mp4")) {
                VideoBean videoBean = new VideoBean();
                videoBean.setUrl(paths.get(i));
                videoBeanList.add(videoBean);
            }
        }

        for (int i = 0; i < names.size(); i++) {
            videoBeanList.get(i).setName(names.get(i));
        }
        extraIv.postDelayed(new Runnable() {
            @Override
            public void run() {
                VideoCoverUtils.load(MainActivity.this,extraIv,videoBeanList.get(0).getUrl());
            }
        },200);
        mediaPlayer.setVideoList(videoBeanList);
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
