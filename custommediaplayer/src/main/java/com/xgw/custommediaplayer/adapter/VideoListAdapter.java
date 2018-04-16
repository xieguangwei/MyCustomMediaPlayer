package com.xgw.custommediaplayer.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xgw.custommediaplayer.R;
import com.xgw.custommediaplayer.entity.VideoBean;
import com.xgw.custommediaplayer.utils.MyMediaPlayManager;
import com.xgw.custommediaplayer.utils.SizeUtils;
import com.xgw.custommediaplayer.utils.VideoCoverUtils;

import java.util.List;

/**
 * Created by XieGuangwei on 2018/3/16.
 */

public class VideoListAdapter extends BaseQuickAdapter<VideoBean, BaseViewHolder> {
    public VideoListAdapter(@Nullable List<VideoBean> data) {
        super(R.layout.item_video, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoBean item) {
        helper.setVisible(R.id.mongli_view, !item.isPlaying())
                .setVisible(R.id.center_name_tv, !item.isPlaying())
                .setVisible(R.id.bottom_name_tv, item.isPlaying()).setText(R.id.center_name_tv, item.getName())
                .setText(R.id.bottom_name_tv, item.getName()).addOnClickListener(R.id.item_video_iv);
        RelativeLayout parentRootRl = helper.getView(R.id.parent_root_rl);
        RelativeLayout childRootRl = helper.getView(R.id.child_root_rl);
        setParams(parentRootRl, childRootRl);

        VideoCoverUtils.load(mContext, (ImageView) helper.getView(R.id.item_video_iv), TextUtils.isEmpty(item.getCoverUrl()) ? item.getUrl() : item.getCoverUrl());
    }

    private void setParams(RelativeLayout parentRl, RelativeLayout childRl) {
        int parentWidth, parentHeight, childWidth, childHeight;
        if (MyMediaPlayManager.isIsFullScreen()) {
            childWidth = 150;
            childHeight = 110;

            parentWidth = 160;
            parentHeight = 120;
        } else {
            childWidth = 110;
            childHeight = 70;

            parentWidth = 120;
            parentHeight = 80;
        }
        //parent
        RecyclerView.LayoutParams parentParams = (RecyclerView.LayoutParams) parentRl.getLayoutParams();
        parentParams.width = SizeUtils.dp2px(parentWidth);
        parentParams.height = SizeUtils.dp2px(parentHeight);
        parentRl.setLayoutParams(parentParams);
        //child
        RelativeLayout.LayoutParams childParams = (RelativeLayout.LayoutParams) childRl.getLayoutParams();
        childParams.width = SizeUtils.dp2px(childWidth);
        childParams.height = SizeUtils.dp2px(childHeight);
        childRl.setLayoutParams(childParams);
    }

    public void setPlay(final String url, final View view) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getData().size(); i++) {
                    getData().get(i).setPlaying(url.equals(getData().get(i).getUrl()));
                }

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
