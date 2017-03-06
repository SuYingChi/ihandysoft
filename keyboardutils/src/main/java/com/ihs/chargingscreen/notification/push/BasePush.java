package com.ihs.chargingscreen.notification.push;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by zhixiangxiao on 5/16/16.
 */
public abstract class BasePush extends LinearLayout {

    public static final int PUSH_DURATION = 5000;
    public static final int PUSH_APPEAR_OFFSET = 100;
    public static final int PUSH_APPEAR_DURATION = 400;

    protected View rootView;

    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;

    protected Handler handler = new Handler();

    protected ObjectAnimator appearAnimator;

    public abstract
    @LayoutRes
    int getRootViewLayoutId();

    public abstract void initLayoutParamsGravityAndType();

    protected abstract void startPushAppearAnimation();

    protected abstract void cancelAnimation();

    public abstract void updatePush();

    @TargetApi(11)
    public BasePush(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @TargetApi(21)
    public BasePush(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public BasePush(Context context) {
        super(context);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initLayoutParams();

        rootView = LayoutInflater.from(context).inflate(getRootViewLayoutId(), this);
    }


    private void initLayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.RGBA_8888;

        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.windowAnimations = android.R.style.Animation_Toast;

        if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        layoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        initLayoutParamsGravityAndType();

    }

    public boolean show() {
        return show(true);
    }

    public boolean show(boolean autoHide) {

        rootView.setVisibility(INVISIBLE);

        try {
               windowManager.addView(this, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setVisibility(VISIBLE);
                        startPushAppearAnimation();
                    }
                }, PUSH_APPEAR_OFFSET);

                if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

            }
        });

        if (autoHide) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            }, PUSH_DURATION);
        }

        return true;
    }

    public void hide() {

        cancelAnimation();

        try {
            windowManager.removeViewImmediate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.removeCallbacksAndMessages(null);

    }

}
