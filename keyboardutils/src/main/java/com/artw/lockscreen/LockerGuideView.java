package com.artw.lockscreen;

import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.ihs.keyboardutils.R;

public class LockerGuideView extends RelativeLayout implements View.OnClickListener {
    private static final int MAX_HAND_SWIPE_ANIMATION_COUNT = 2;

    public interface OnFinishListener {
        void onFinish();
    }

    private OnFinishListener listener;

    private int cameraHandSwipeAnimationCount;
    private int controlCenterHandSwipeAnimationCount;

    public LockerGuideView(Context context) {
        this(context, null);
    }

    public LockerGuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(null);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                doCameraHighlightBackgroundAnimation();
            }
        }, 500);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View btnNext = findViewById(R.id.tv_camera_confirm_btn);
        btnNext.setOnClickListener(this);
        View btnGotIt = findViewById(R.id.tv_control_center_confirm_btn);
        btnGotIt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_camera_confirm_btn) {
            cameraHandSwipeAnimationCount = MAX_HAND_SWIPE_ANIMATION_COUNT;
            switchToControlCenterGuideView();
        } else if (v.getId() == R.id.tv_control_center_confirm_btn) {
            controlCenterHandSwipeAnimationCount = MAX_HAND_SWIPE_ANIMATION_COUNT;
            finish();
        }
    }

    private void doCameraHighlightBackgroundAnimation() {
        final View view = findViewById(R.id.camera_highlight_bg);

        ScaleAnimation sa = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(1000);
        sa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doCameraTextAndArrowAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(sa);
    }

    private void doCameraTextAndArrowAnimation() {
        final View view = findViewById(R.id.iv_camera_arrow);
        final View tipsContainer = findViewById(R.id.camera_tips_container);

        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(300);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
                tipsContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doCameraHandSwipeAnimation(findViewById(R.id.camera_hand));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tipsContainer.startAnimation(aa);
        view.startAnimation(aa);
    }

    private void doControlCenterAppearAnimation() {
        findViewById(R.id.guide_control_center).setVisibility(View.VISIBLE);

        final View tipsContainer = findViewById(R.id.control_center_tips_container);
        final View arrowView = findViewById(R.id.iv_control_center_arrow);
        final View function1 = findViewById(R.id.function1);
        final View function2 = findViewById(R.id.function2);
        final View function3 = findViewById(R.id.function3);

        AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setDuration(300);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doControlCenterHandSwipeAnimation(findViewById(R.id.iv_control_center_hand));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // function button animation
        ScaleAnimation sa = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(300);

        tipsContainer.startAnimation(aa);
        arrowView.startAnimation(aa);

        function1.startAnimation(sa);
        function2.startAnimation(sa);
        function3.startAnimation(sa);
    }

    private void doCameraHandSwipeAnimation(final View view) {
        if (cameraHandSwipeAnimationCount >= MAX_HAND_SWIPE_ANIMATION_COUNT) {
            cameraHandSwipeAnimationCount = 0;
            return;
        }

        cameraHandSwipeAnimationCount++;

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -3.5f);
        animation.setDuration(1500);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // dismiss animation
        AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(300);
        aa.setStartOffset(2500);
        aa.setFillAfter(true);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Should reset alpha otherwise on some device will become invisible because transparent
                view.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(animation);
        as.addAnimation(aa);
        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doCameraHandSwipeAnimation(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(as);
    }

    private void doControlCenterHandSwipeAnimation(final View view) {
        if (controlCenterHandSwipeAnimationCount >= MAX_HAND_SWIPE_ANIMATION_COUNT) {
            controlCenterHandSwipeAnimationCount = 0;
            return;
        }

        controlCenterHandSwipeAnimationCount++;

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -3.5f);
        animation.setDuration(1500);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // dismiss animation
        AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(300);
        aa.setStartOffset(2500);
        aa.setFillAfter(true);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Should reset alpha otherwise on some device will become invisible because transparent
                view.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet as = new AnimationSet(true);
        as.addAnimation(animation);
        as.addAnimation(aa);
        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doControlCenterHandSwipeAnimation(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(as);
    }

    private void switchToControlCenterGuideView() {
        final View guideCamera = findViewById(R.id.guide_camera);

        AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(300);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                guideCamera.setVisibility(View.INVISIBLE);
                doControlCenterAppearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        guideCamera.startAnimation(aa);
    }

    private void finish() {
        AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(300);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) {
                    listener.onFinish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(aa);
    }

    public void setOnFinishListener(OnFinishListener listener) {
        this.listener = listener;
    }
}
