package com.xgw.custommediaplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

/**
 * Created by XieGuangwei on 2018/4/12.
 */

public class MyTextureView extends TextureView{
    private int videoHeight;
    private int videoWidth;

    public MyTextureView(Context context) {
        super(context);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void adaptVideoSize(int videoWidth, int videoHeight) {
        if(this.videoWidth != videoWidth && this.videoHeight != videoHeight) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.requestLayout();
        }

    }

    public void setRotation(float rotation) {
        if(rotation != this.getRotation()) {
            super.setRotation(rotation);
            this.requestLayout();
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float viewRotation = this.getRotation();
        int width;
        if(viewRotation == 90.0F || viewRotation == 270.0F) {
            width = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = width;
        }

        width = getDefaultSize(this.videoWidth, widthMeasureSpec);
        int height = getDefaultSize(this.videoHeight, heightMeasureSpec);
        if(this.videoWidth > 0 && this.videoHeight > 0) {
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
            if(widthSpecMode == 1073741824 && heightSpecMode == 1073741824) {
                width = widthSpecSize;
                height = heightSpecSize;
                if(this.videoWidth * heightSpecSize < widthSpecSize * this.videoHeight) {
                    width = heightSpecSize * this.videoWidth / this.videoHeight;
                } else if(this.videoWidth * heightSpecSize > widthSpecSize * this.videoHeight) {
                    height = widthSpecSize * this.videoHeight / this.videoWidth;
                }
            } else if(widthSpecMode == 1073741824) {
                width = widthSpecSize;
                height = widthSpecSize * this.videoHeight / this.videoWidth;
                if(heightSpecMode == -2147483648 && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = heightSpecSize * this.videoWidth / this.videoHeight;
                }
            } else if(heightSpecMode == 1073741824) {
                height = heightSpecSize;
                width = heightSpecSize * this.videoWidth / this.videoHeight;
                if(widthSpecMode == -2147483648 && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = widthSpecSize * this.videoHeight / this.videoWidth;
                }
            } else {
                width = this.videoWidth;
                height = this.videoHeight;
                if(heightSpecMode == -2147483648 && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = heightSpecSize * this.videoWidth / this.videoHeight;
                }

                if(widthSpecMode == -2147483648 && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = widthSpecSize * this.videoHeight / this.videoWidth;
                }
            }
        }

        this.setMeasuredDimension(width, height);
    }
}
