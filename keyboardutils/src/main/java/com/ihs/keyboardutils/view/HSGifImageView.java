package com.ihs.keyboardutils.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 16/11/3.
 */

public class HSGifImageView extends FrameLayout {
    private GifImageView mGifImageView;

    public HSGifImageView(Context context) {
        this(context,null);
    }

    public HSGifImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HSGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        mGifImageView = new GifImageView(getContext());
        addView(mGifImageView,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setImageURI(Uri uri){
        mGifImageView.setImageURI(uri);
    }

    public void setImageResource(int resId){
        mGifImageView.setImageResource(resId);
    }

    public void setImageDrawable(Drawable drawable){
        mGifImageView.setImageDrawable(drawable);
    }

    public void setScaleType(ImageView.ScaleType scaleType){
        mGifImageView.setScaleType(scaleType);
    }

    public ImageView.ScaleType getScaleType() {
        return mGifImageView.getScaleType();
    }

    public void stop(){
        GifDrawable gifDrawable = getGifDrawable();
        if(gifDrawable!=null){
            gifDrawable.stop();
        }
    }

    public void start(){
        GifDrawable gifDrawable = getGifDrawable();
        if(gifDrawable!=null){
            gifDrawable.start();
        }
    }

    /**
     * recycle gif drawable and set image drawable null
     */
    public void clear(){
        GifDrawable gifDrawable = getGifDrawable();
        if(gifDrawable!=null){
            gifDrawable.recycle();
        }
        mGifImageView.setImageDrawable(null);
    }

    private GifDrawable getGifDrawable(){
        Drawable drawable = mGifImageView.getDrawable();
        if(drawable!=null){
            if(drawable instanceof GifDrawable){
                return (GifDrawable) drawable;
            }
        }
        return null;
    }
}