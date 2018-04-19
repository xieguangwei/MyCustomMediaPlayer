package com.xgw.custommediaplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.socks.library.KLog;
import com.xgw.custommediaplayer.R;
import com.xgw.custommediaplayer.adapter.VideoListAdapter;
import com.xgw.custommediaplayer.constants.PlayMode;
import com.xgw.custommediaplayer.constants.ScreenStatus;
import com.xgw.custommediaplayer.entity.VideoBean;
import com.xgw.custommediaplayer.listener.VideoPlayListener;
import com.xgw.custommediaplayer.utils.MyMediaPlayManager;
import com.xgw.custommediaplayer.utils.MyMediaPlayerDelegate;
import com.xgw.custommediaplayer.utils.MyVideoControlManager;
import com.xgw.custommediaplayer.utils.PlayModeUtils;
import com.xgw.custommediaplayer.utils.VideoCoverUtils;
import com.xgw.custommediaplayer.videogesture.ShowControlLayout;
import com.xgw.custommediaplayer.videogesture.VideoGestureLayout;

import java.io.File;
import java.util.List;

/**
 * Created by XieGuangwei on 2018/4/2.
 * MediaPlayer+SurfaceView实现视频播放（替换原生官方封装的videoview）
 * 灵感来自https://github.com/JackChan1999
 */

public class MyMediaPlayer extends RelativeLayout implements View.OnClickListener, CacheListener {
    private MyTextureView textureView;//MediaPlayer显示在其上
    private Button btnPlay;//暂停、播放按钮

    private MyCoverImageView coverIv;//封面
    private RelativeLayout loadingRl;//加载loading布局
    private RelativeLayout reloadRl;//重新加载布局

    private TextView durationTv;//显示总时长
    private TextView playStartTv;//显示当前进度市场
    private SeekBar mSeekBar;//进度条
    private ImageView fullScreenIv;//全屏按钮
    private ImageView playModeIv;//播放模式按钮

    private LinearLayout playProgressLl;//底部进度布局

    private int currentProgress;

    private VideoPlayListener listener;

    private static final int UPDATE_TIME_AND_PROGRESS = 1;//更新时间和进度的消息

    private String playUrl;//当前播放url


    private MediaPlayer mPlayer;

    private Surface mSurface;

    private boolean hasPaused;//是否执行了暂停操作

    private boolean hasTouchedListOrSeekBar = false;

    //当前屏幕状态，默认为正常
    private ScreenStatus screenStatus = ScreenStatus.SCREEN_STATUS_NORMAL;
    private HideControlRunnable mHideRunnable;

    private VideoGestureLayout vgl;//手势控制布局（控制音量、亮度、进度）
    private ShowControlLayout scl;//显示手势控制的信息布局

    //视频列表布局
    private RelativeLayout videoListRl;
    //视频列表
    private RecyclerView videoListRecyclerView;
    private VideoListAdapter videoListAdapter;

    private boolean isTextureAvailable = false;
    private boolean isFirstEnter = true;

    private String coverUrl;//封面url

    /**
     * handler每隔500ms刷新一次当前播放进度
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case UPDATE_TIME_AND_PROGRESS://更新进度和时间
                    if (mPlayer == null) {
                        return;
                    }
                    //获取当前时间
                    int currentTime = mPlayer.getCurrentPosition();
                    currentProgress = mPlayer.getCurrentPosition();
                    //获取总时间
                    int totalTime = mPlayer.getDuration();

                    //设置总时间、当前时间显示，并格式化
                    updateTimeFormat(durationTv, totalTime);
                    updateTimeFormat(playStartTv, currentTime);

                    //设置进度条
                    mSeekBar.setMax(totalTime);
                    mSeekBar.setProgress(currentTime);

                    //每隔500ms通知自己刷新一次进度
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME_AND_PROGRESS, 500);//500MS通知自己刷新一次
                    break;
            }
        }
    };

    /**
     * 时间格式化
     *
     * @param textView    时间控件
     * @param millisecond 总时间 毫秒
     */
    private void updateTimeFormat(TextView textView, int millisecond) {
        //将毫秒转换为秒
        int second = millisecond / 1000;
        //计算小时
        int hh = second / 3600;
        //计算分钟
        int mm = second % 3600 / 60;
        //计算秒
        int ss = second % 60;
        //判断时间单位的位数
        String str = null;
        if (hh != 0) {//表示时间单位为三位
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        //将时间赋值给控件
        textView.setText(str);
    }

    public void setController(Activity activity, MyVideoControlManager myVideoControlManager) {
        myVideoControlManager.bindView(activity, this, vgl, scl);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == btnPlay.getId()) {
            //开始/暂停
            playOrPause();
        } else if (id == reloadRl.getId()) {
            //播放出错，重新加载
            playSpecifiedVideo(playUrl, coverUrl);
        } else if (id == fullScreenIv.getId()) {
            //全屏/退出全屏
            if (listener != null) {
                if (screenStatus == ScreenStatus.SCREEN_STATUS_FULL) {
                    listener.onExitFullScreen();
                } else {
                    listener.onStartFullScreen();
                }
            }
        } else if (id == playModeIv.getId()) {
            //播放模式
            setPlayMode();
        }
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        if (listener != null) {
            listener.onCacheProgress(playUrl, percentsAvailable);
        }
        //缓存进度回调,设置第二个进度条
        int secondProgress = (int) (mSeekBar.getMax() * (percentsAvailable / 100f));
        mSeekBar.setSecondaryProgress(secondProgress);
    }

    //隐藏control布局的Runnable
    private class HideControlRunnable implements Runnable {
        @Override
        public void run() {
            if (MyMediaPlayManager.getInstance().isPlaying()) {
                if (!hasTouchedListOrSeekBar) {
                    setControlVisibility(GONE, GONE, GONE);
                }
            }
        }
    }

    public MyMediaPlayer(Context context) {
        super(context);
        init(context);
    }

    public MyMediaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyMediaPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        isFirstEnter = true;
        View contentView = LayoutInflater.from(context).inflate(R.layout.my_media_player_layout, this, true);
        //初始化控件
        textureView = (MyTextureView) contentView.findViewById(R.id.texture_view);
        //TextureView创建监听实现四个方法
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        //开始/暂停
        btnPlay = (Button) contentView.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        //封面
        coverIv = (MyCoverImageView) contentView.findViewById(R.id.cover_iv);

        //加载布局
        loadingRl = (RelativeLayout) contentView.findViewById(R.id.loading_rl);

        //重新加载布局
        reloadRl = (RelativeLayout) contentView.findViewById(R.id.reload_rl);
        reloadRl.setOnClickListener(this);

        //总时长
        durationTv = (TextView) contentView.findViewById(R.id.duration_tv);
        //当前时长
        playStartTv = (TextView) contentView.findViewById(R.id.play_start_tv);

        //进度条
        mSeekBar = (SeekBar) contentView.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //拖动视频是禁止刷新
                mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //停止拖动，获取总进度
                int totalTime = mSeekBar.getProgress();
                //跳转到当前位置
                seekTo(totalTime);
                //重新刷新
                mHandler.sendEmptyMessage(UPDATE_TIME_AND_PROGRESS);
            }
        });
        //进度条布局
        playProgressLl = (LinearLayout) contentView.findViewById(R.id.play_progress);

        //全屏按钮
        fullScreenIv = (ImageView) contentView.findViewById(R.id.full_screen_iv);
        fullScreenIv.setOnClickListener(this);

        //播放模式按钮
        playModeIv = (ImageView) contentView.findViewById(R.id.play_model_iv);
        playModeIv.setOnClickListener(this);


        //手势相关
        vgl = (VideoGestureLayout) contentView.findViewById(R.id.vgl);
        scl = (ShowControlLayout) contentView.findViewById(R.id.scl);

        //列表相关
        videoListRl = (RelativeLayout) findViewById(R.id.video_list_rl);
        videoListRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        videoListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        videoListAdapter = new VideoListAdapter(null);
        videoListRecyclerView.setAdapter(videoListAdapter);

        videoListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                VideoBean videoBean = (VideoBean) adapter.getItem(position);
                if (videoBean == null) {
                    return;
                }
                String url = videoBean.getUrl();
                String coverUrl = videoBean.getCoverUrl();
                videoListAdapter.setPlay(url, MyMediaPlayer.this);
                currentProgress = 0;
                playSpecifiedVideo(url, coverUrl);
            }
        });

        setPlayModelImage();


        mHideRunnable = new HideControlRunnable();

        videoListRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    hasTouchedListOrSeekBar = true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mHandler.removeCallbacks(mHideRunnable);
                    mHandler.postDelayed(mHideRunnable, MyMediaPlayerDelegate.getInstance().getDelayMillis() / 2);
                    hasTouchedListOrSeekBar = false;
                }
                return false;
            }
        });

        mSeekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    hasTouchedListOrSeekBar = true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mHandler.removeCallbacks(mHideRunnable);
                    mHandler.postDelayed(mHideRunnable, MyMediaPlayerDelegate.getInstance().getDelayMillis() / 2);
                    hasTouchedListOrSeekBar = false;
                }
                return false;
            }
        });
    }

    /**
     * 设置视频资源列表
     */
    public void setVideoList(List<VideoBean> videoList) {
        if (videoListAdapter != null && videoList != null && videoList.size() > 0) {
            videoListAdapter.setNewData(videoList);
            for (int i = 0; i < videoList.size(); i++) {
                VideoBean item = videoList.get(i);
                if (item.isPlaying()) {
                    setPlayUrl(item.getUrl(), item.getCoverUrl());
                    break;
                }
            }
            if (TextUtils.isEmpty(playUrl)) {
                setPlayUrl(videoList.get(0).getUrl(), videoList.get(0).getCoverUrl());
                videoListAdapter.setPlay(playUrl, this);
            }
        }
        loadCoverUrl();
    }

    private void setPlayUrl(String playUrl, String coverUrl) {
        this.playUrl = playUrl;
        this.coverUrl = TextUtils.isEmpty(coverUrl) ? playUrl : coverUrl;
    }

    //TextureView创建监听
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            KLog.e("onSurfaceTextureAvailable");
            mSurface = new Surface(surfaceTexture);//连接MediaPlayer和TextureView两个对象
            isTextureAvailable = true;
            //设置第一次打开时的播放器
            playSpecifiedVideo(playUrl, coverUrl);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            isTextureAvailable = false;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    public void seekTo(int progress) {
        MyMediaPlayManager.seekTo(progress);
    }

    /**
     * 设置播放器播放监听回调（当前只对播放到哪一个视频进行了监听）
     *
     * @param listener
     */
    public void setVideoPlayListener(VideoPlayListener listener) {
        this.listener = listener;
    }

    /**
     * 加载封面
     */
    private void loadCoverUrl() {
        VideoCoverUtils.load(getContext(), coverIv, coverUrl);
    }

    /**
     * 给一个初始化的播放url
     *
     * @param url
     */
    public void initPlayUrl(String url, String coverUrl) {
        setPlayUrl(url, coverUrl);
        //没设置正在播放的视频，默认播放第一个视频
        if (TextUtils.isEmpty(playUrl)) {
            onErrorHandle(new Exception("initPlayUrl时视频地址为空！！！"));
            return;
        }

        try {
            //设置uri、封面
            loadCoverUrl();
            setControlVisibility(GONE, GONE, GONE);
        } catch (Exception e) {
            KLog.e(e.getMessage());
            e.printStackTrace();
            onErrorHandle(e);
        }
    }

    /**
     * 播放出错时的处理
     *
     * @param e
     */
    private void onErrorHandle(Exception e) {
        showReload();
        stopLoading();
        mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
        btnPlay.setBackgroundResource(R.drawable.play_start);
        if (listener != null) {
            listener.onError(e);
        }
    }

    /**
     * 设置播放列表
     *
     * @param url
     */
    public void playSpecifiedVideo(String url, String coverUrl) {
        setPlayUrl(url, coverUrl);
        //没设置正在播放的视频，默认播放第一个视频
        if (TextUtils.isEmpty(playUrl)) {
            onErrorHandle(new Exception("playSpecifiedVideo时视频地址为空!!!"));
            return;
        }

        try {
            //设置uri、封面
            loadCoverUrl();
            coverIv.setVisibility(VISIBLE);
            if (mHandler != null) {
                mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
            }
            openMediaPlayer();
            setControlVisibility(GONE, GONE, GONE);
        } catch (Exception e) {
            KLog.e(e.getMessage());
            e.printStackTrace();
            onErrorHandle(e);

        }
    }

    //播放准备完成
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            onPreparedHandle();
        }
    };

    /**
     * 准备完成的处理
     */
    private void onPreparedHandle() {
        //准备播放
        //隐藏视频加载进度框
        KLog.e("onPrepared");
        //进行视频的播放
        MyMediaPlayManager.play();
        videoListAdapter.setPlay(playUrl, MyMediaPlayer.this);
        mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
        mHandler.sendEmptyMessage(UPDATE_TIME_AND_PROGRESS);
        btnPlay.setBackgroundResource(R.drawable.play_pause);
        if (currentProgress > 0) {
            seekTo(currentProgress);
        }
        stopLoading();
        dismissReload();
        setControlVisibility(GONE, GONE, GONE);
        coverIv.setVisibility(GONE);
        if (listener != null) {
            listener.onPrepared(playUrl);
        }
    }

    //缓冲，设置第二个进度条
    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
//            onBufferingUpdateHandle(percent);
        }
    };

    /**
     * 缓存进度条更新处理
     *
     * @param percent
     */
    private void onBufferingUpdateHandle(int percent) {
        KLog.e("设置第二条进度：" + percent);
        int secondProgress = (int) (mSeekBar.getMax() * (percent / 100f));
        mSeekBar.setSecondaryProgress(secondProgress);
    }

    //播放完成
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            //回调该方法说明播单个视频完成
            onCompleteHandle();
        }
    };

    /**
     * 播放完成时的处理
     */
    private void onCompleteHandle() {
        KLog.e("onCompletion");
//            coverIv.setVisibility(VISIBLE);
        btnPlay.setBackgroundResource(R.drawable.play_start);
        setControlVisibility(VISIBLE, GONE, GONE);
        mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
        currentProgress = 0;
        if (listener != null) {
            listener.onCompletion();
        }

        KLog.e("onCompletion");
        coverIv.setVisibility(VISIBLE);
        mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
        int playMode = PlayModeUtils.getPlayMode();
        //单曲循环模式---一直播放该视频
        if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_SINGLE_CYCLE) {
            playSpecifiedVideo(playUrl, coverUrl);
        } else if (playMode == PlayMode.PLAY_MODE_LIST_CYCLE) {//列表循环模式---播放完最后一个视频播放第一个视频
            for (int i = 0; i < videoListAdapter.getData().size(); i++) {
                VideoBean videoBean = videoListAdapter.getData().get(i);
                if (videoBean != null && videoBean.isPlaying()) {
                    if (i < videoListAdapter.getData().size() - 1) {
                        playSpecifiedVideo(videoListAdapter.getData().get(i + 1).getUrl(), videoListAdapter.getData().get(i + 1).getCoverUrl());
                    } else {
                        playSpecifiedVideo(videoListAdapter.getData().get(0).getUrl(), videoListAdapter.getData().get(0).getCoverUrl());
                    }
                    break;
                }
            }
        } else {//顺序播放-----播放完最后一个视频不再继续播放，点击播放按钮后再继续播放最后一个视频
            for (int i = 0; i < videoListAdapter.getData().size(); i++) {
                VideoBean videoBean = videoListAdapter.getData().get(i);
                if (videoBean != null && videoBean.isPlaying()) {
                    if (i < videoListAdapter.getData().size() - 1) {
                        playSpecifiedVideo(videoListAdapter.getData().get(i + 1).getUrl(), videoListAdapter.getData().get(i + 1).getCoverUrl());
                    } else {
                        coverIv.setVisibility(VISIBLE);
                        btnPlay.setVisibility(VISIBLE);
                    }
                    break;
                }
            }
        }
    }

    //播放失败
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            onErrorHandle(new Exception("错误码：(" + i + "," + i1 + ")"));
            return false;
        }
    };

    //相关信息监听，此处主要获取是否正在加载
    private MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                //开始loading，显示菊花
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    KLog.e("显示菊花");
                    startLoading();
                    break;
                //loading完成，隐藏菊花
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    KLog.e("隐藏菊花");
                    stopLoading();
                    break;
            }
            return false;
        }
    };

    //视频大小变化
    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
            //适配大小
            textureView.adaptVideoSize(width, height);
        }
    };

    //视频播放（视频的初始化）
    private void openMediaPlayer() {
        KLog.e("openMediaPlayer");
        try {
            releaseMediaPlayer();

            startLoading();
            mPlayer = MyMediaPlayManager.getInstance();
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //注册缓存策略
            String proxyUrl = registerCache();
            //设置代理播放地址
            mPlayer.setDataSource(proxyUrl);
            //让MediaPlayer和TextureView进行视频画面的结合
            mPlayer.setSurface(mSurface);
            //设置监听
            mPlayer.setOnCompletionListener(onCompletionListener);
            mPlayer.setOnErrorListener(onErrorListener);
            mPlayer.setOnPreparedListener(onPreparedListener);
            mPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
            mPlayer.setOnInfoListener(onInfoListener);
            mPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
            mPlayer.setScreenOnWhilePlaying(true);//在视频播放的时候保持屏幕的高亮
            //异步准备
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            onErrorHandle(e);
        }
    }

    /**
     * 注册缓存，并返回代理地址
     *
     * @return
     */
    private String registerCache() {
        //检测缓存状态
        checkCachedState();
        //开启注册缓存策略
        HttpProxyCacheServer proxy = MyMediaPlayerDelegate.getProxy();
        proxy.registerCacheListener(this, playUrl);
        return proxy.getProxyUrl(playUrl);
    }

    /**
     * 取消缓存注册
     */
    private void unRegisterCache() {
        MyMediaPlayerDelegate.getProxy().unregisterCacheListener(this);
    }

    /**
     * 暂停/播放
     */
    public void playOrPause() {
        if (MyMediaPlayManager.getInstance().isPlaying()) {
            MyMediaPlayManager.pause();
            btnPlay.setBackgroundResource(R.drawable.play_start);
            hasPaused = true;
            setControlVisibility(VISIBLE, VISIBLE, VISIBLE);
        } else {
            setControlVisibility(GONE, GONE, GONE);
            if (hasPaused) {
                btnPlay.setBackgroundResource(R.drawable.play_pause);
                //继续播放
                MyMediaPlayManager.play();
                hasPaused = false;
            } else {
                btnPlay.setBackgroundResource(R.drawable.play_pause);
                //开始播放（新的）
                //显示TextureView，回调onSurfaceTextureAvailable方法，开始执行新的播放操作
                playSpecifiedVideo(playUrl, coverUrl);
            }
            setControlVisibility(GONE, GONE, GONE);
        }
    }

    /**
     * 控制布局显示、隐藏
     */
    public void setControlVisibility(int btnPlayVisibility, int playProgressVisibility, int videoListVisibility) {
        btnPlay.setVisibility(btnPlayVisibility);
        playProgressLl.setVisibility(playProgressVisibility);
        videoListRl.setVisibility(videoListVisibility);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, MyMediaPlayerDelegate.getInstance().getDelayMillis());
    }

    public boolean isControlVisibility() {
        return btnPlay != null && playProgressLl != null && videoListRl != null && btnPlay.getVisibility() == VISIBLE && playProgressLl.getVisibility() == VISIBLE && videoListRl.getVisibility() == VISIBLE;
    }

    /**
     * 开始加载
     */
    private void startLoading() {
        loadingRl.setVisibility(VISIBLE);
        setControlVisibility(GONE, GONE, GONE);
        dismissReload();
    }

    /**
     * 停止加载
     */
    private void stopLoading() {
        loadingRl.setVisibility(GONE);
        setControlVisibility(GONE, GONE, GONE);
    }

    /**
     * 显示重新加载布局
     */
    private void showReload() {
        reloadRl.setVisibility(VISIBLE);
    }

    /**
     * 隐藏重新加载布局
     */
    private void dismissReload() {
        reloadRl.setVisibility(GONE);
    }


    /**
     * 设置播放模式
     */
    private void setPlayMode() {
        String toast = null;
        //顺序播放、列表循环、单曲循环
        if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_LIST_ORDER) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_LIST_CYCLE);
            toast = "列表循环";
        } else if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_LIST_CYCLE) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_SINGLE_CYCLE);
            toast = "单曲循环";
        } else if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_SINGLE_CYCLE) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_LIST_ORDER);
            toast = "顺序播放";
        }
        Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
        setPlayModelImage();
    }

    /**
     * 设置playmodel对应的按钮
     */
    private void setPlayModelImage() {
        if (playModeIv == null) {
            return;
        }
        int resourceId;
        switch (PlayModeUtils.getPlayMode()) {
            case PlayMode.PLAY_MODE_LIST_ORDER:
                resourceId = R.drawable.ic_play_model_order;
                break;
            case PlayMode.PLAY_MODE_LIST_CYCLE:
                resourceId = R.drawable.ic_play_model_list_recycle;
                break;
            case PlayMode.PLAY_MODE_SINGLE_CYCLE:
                resourceId = R.drawable.ic_play_model_single_recycle;
                break;
            default:
                resourceId = R.drawable.ic_play_model_order;
                break;
        }
        playModeIv.setImageResource(resourceId);
    }

    /**
     * 设置全屏、退出全屏后的参数、状态
     *
     * @param screenStatus
     */
    public void setScreenParams(ScreenStatus screenStatus) {
        if (fullScreenIv != null) {
            this.screenStatus = screenStatus;
            if (screenStatus == ScreenStatus.SCREEN_STATUS_FULL) {
                fullScreenIv.setImageResource(R.drawable.video_exit);
            } else if (screenStatus == ScreenStatus.SCREEN_STATUS_NORMAL) {
                fullScreenIv.setImageResource(R.drawable.video_full_screen);
            }
            MyMediaPlayManager.setIsFullScreen(screenStatus == ScreenStatus.SCREEN_STATUS_FULL);
            videoListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 检测当前缓存状态，如果缓存完成，则把第二个进度条填满
     */
    private void checkCachedState() {
        HttpProxyCacheServer proxy = MyMediaPlayerDelegate.getProxy();
        boolean fullyCached = proxy.isCached(playUrl);
        if (fullyCached) {
            mSeekBar.setSecondaryProgress(mSeekBar.getMax());
        }
    }

    /**
     * 组件onPause中调用该方法（暂停播放）
     */
    public void onPause() {
        releaseMediaPlayer();
        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
        }
        btnPlay.setBackgroundResource(R.drawable.play_start);
        coverIv.setVisibility(VISIBLE);
        setControlVisibility(VISIBLE, VISIBLE, VISIBLE);
    }

    /**
     * 释放播放器
     */
    private void releaseMediaPlayer() {
        MyMediaPlayManager.release();
        //释放播放器后取消缓存注册
        unRegisterCache();
    }

    /**
     * 组件onResume中调用该方法（恢复播放）
     */
    public void onResume() {
        KLog.e("onResume");
        setPlayModelImage();
        if (!isFirstEnter && isTextureAvailable) {
            btnPlay.setBackgroundResource(R.drawable.play_pause);
            playSpecifiedVideo(playUrl, coverUrl);
        }
        isFirstEnter = false;
    }

    /**
     * 按返回按钮调用
     *
     * @return
     */
    public boolean onBackPressed() {
        if (listener != null && screenStatus == ScreenStatus.SCREEN_STATUS_FULL) {
            listener.onExitFullScreen();
            return false;
        } else {
            return true;
        }
    }
}
