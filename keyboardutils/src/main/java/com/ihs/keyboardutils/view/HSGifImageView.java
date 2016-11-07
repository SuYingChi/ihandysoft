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
        super(context);
        initView(null);
    }

    public HSGifImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        initView(attrs);
    }

    public HSGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        mGifImageView = new GifImageView(getContext(),attrs);
        addView(mGifImageView,new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setImageURI(Uri uri){
        mGifImageView.setImageURI(uri);
    }

    public void setImageResource(int resId){
        mGifImageView.setImageResource(resId);
    }

    public void setScaleType(ImageView.ScaleType scaleType){
        mGifImageView.setScaleType(scaleType);
    }

    public void setImageDrawable(Drawable drawable){
        mGifImageView.setImageDrawable(drawable);
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
    private void release(){
        GifDrawable gifDrawable = getGifDrawable();
        if(gifDrawable!=null){
            gifDrawable.start();
        }
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


    private void onAttach() {

    }

    /**
     * handle view detach from window behaviour,inspiration from fresco
     */
    private void onDetach(){
        release();
        setImageDrawable(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttach();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        onDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        onAttach();
    }
}