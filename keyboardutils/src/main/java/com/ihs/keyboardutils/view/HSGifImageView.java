package com.ihs.keyboardutils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.keyboardutils.R;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jixiang on 16/11/3.
 */

public class HSGifImageView extends FrameLayout {
    private GifImageView mGifImageView;

    // Draw round corner
    private Paint imagePaint;
    private Paint roundPaint;
    private float cornerRadius = 0;

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

        // Get corner cornerRadius
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.GifImageView);

            if (ta != null) {
                cornerRadius = ta.getDimension(R.styleable.GifImageView_corner_radius, 0);
                ta.recycle();
            }
        }

        // Draw round corner
        if (cornerRadius > 0) {
            roundPaint = new Paint();
            roundPaint.setColor(Color.WHITE);
            roundPaint.setAntiAlias(true);
            roundPaint.setStyle(Paint.Style.FILL);
            roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            imagePaint = new Paint();
            imagePaint.setXfermode(null);
        }
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

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (cornerRadius > 0) {
            canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);
            super.dispatchDraw(canvas);

            // Draw round corner
            drawTopLeft(canvas);
            drawTopRight(canvas);
            drawBottomLeft(canvas);
            drawBottomRight(canvas);

            canvas.restore();
        } else {
            super.dispatchDraw(canvas);
        }
    }

    private void drawTopLeft(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, cornerRadius);
        path.lineTo(0, 0);
        path.lineTo(cornerRadius, 0);
        path.arcTo(new RectF(0, 0, cornerRadius * 2, cornerRadius * 2), -90, -90);
        path.close();
        canvas.drawPath(path, roundPaint);
    }

    private void drawTopRight(Canvas canvas) {
        int width = getWidth();
        Path path = new Path();
        path.moveTo(width - cornerRadius, 0);
        path.lineTo(width, 0);
        path.lineTo(width, cornerRadius);
        path.arcTo(new RectF(width - 2 * cornerRadius, 0, width, cornerRadius * 2), 0, -90);
        path.close();
        canvas.drawPath(path, roundPaint);
    }

    private void drawBottomLeft(Canvas canvas) {
        int height = getHeight();
        Path path = new Path();
        path.moveTo(0, height - cornerRadius);
        path.lineTo(0, height);
        path.lineTo(cornerRadius, height);
        path.arcTo(new RectF(0, height - 2 * cornerRadius, cornerRadius * 2, height), 90, 90);
        path.close();
        canvas.drawPath(path, roundPaint);
    }

    private void drawBottomRight(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        Path path = new Path();
        path.moveTo(width - cornerRadius, height);
        path.lineTo(width, height);
        path.lineTo(width, height - cornerRadius);
        path.arcTo(new RectF(width - 2 * cornerRadius, height - 2 * cornerRadius, width, height), 0, 90);
        path.close();
        canvas.drawPath(path, roundPaint);
    }
}