package com.ihs.feature.ui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.HorizontalBannerImageView;
import com.ihs.feature.common.LauncherConstants;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.ViewUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

public abstract class FloatingDialog extends FloatWindowDialog implements FloatWindowListener {

    private static final long DURATION_ALPHA_IN = 300;
    private static final long DURATION_SLIDE_IN = 480;
    private static final long DURATION_SLIDE_OUT = 400;
    private static final long DURATION_ALPHA_OUT = 200;

    private static final int SLIDE_IN_INITIAL_ROTATION_DEGREE = 5;
    private static final float SLIDE_IN_OVERSHOOT_TENSION = 0.8f;

    public static final @ColorInt int BACKGROUND_COLOR = Color.argb(0x82, 0x00, 0x00, 0x00);

    private int mDialogElevation;
    private boolean mIsDialogDismissAnimating;

    protected Activity mActivity;
    private View mBackground;
    protected View mContainer;
    private DialogContentContainer mMainPad;
    protected ImageView mTopImage;

    protected SafeWindowManager mWindow;
    @Thunk
    ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
    @Thunk TimeInterpolator mDecelerateQuart = new DecelerateInterpolator(2f);

    public FloatingDialog(Context context) {
        this(context, false);
    }

    public FloatingDialog(Context context, boolean delayInit) {
        super(context);
        if (!delayInit) {
            init(context);
        }
    }

    public FloatingDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
     * Subclass should override the following methods to give dialog information.
     */

    protected abstract FloatWindowManager.Type getType();

    protected abstract View createDialogContainer(LayoutInflater inflater, ViewGroup root);

    protected abstract View createContentView(LayoutInflater inflater, ViewGroup root);

    protected abstract void findTopImageView();

    protected abstract Drawable getTopImageDrawable();

    protected abstract int getTopImageOverHeight();

    protected boolean isAppearAlphaAnimation() {
        return false;
    }

    protected int getTopImageMarginBottom() {
        return CommonUtils.pxFromDp(10);
    }

    protected float getBannerImageAspectRatio() {
        return 0f;
    }

    protected void onDrawBackground(Canvas canvas) {
    }

    protected void onDismissComplete() {
    }

    protected void onCanceled() {
    }

    protected boolean enableElevation() {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init(Context context) {
        mActivity = (Activity) context;
        mDialogElevation = getResources().getDimensionPixelSize(R.dimen.dialog_elevation);
        LayoutInflater inflater = LayoutInflater.from(context);

        mBackground = createDialogContainer(inflater, this);

        mContainer = ViewUtils.findViewById(mBackground, R.id.floating_dialog_container); // floating_dialog_container used in double_button_dialog.xml and accessibility_authorize_dialog.xml
        mMainPad = ViewUtils.findViewById(mBackground, R.id.tip_container); // tip_container used in double_button_dialog.xml and accessibility_authorize_dialog.xml
        if (CommonUtils.ATLEAST_LOLLIPOP && enableElevation()) {
            mMainPad.setElevation(mDialogElevation);
        }
        final ViewGroup contentView = ViewUtils.findViewById(mMainPad, R.id.content_view);

        mMainPad.setDialog(this);

        contentView.addView(createContentView(inflater, contentView));

        findTopImageView();

        if (enableTopBackground()) {
            mTopImage.setBackground(getTopImageDrawable());
        } else {
            mTopImage.setImageDrawable(getTopImageDrawable());
        }
        if (mTopImage.getDrawable() != null || mTopImage.getBackground() != null) {
            mTopImage.setVisibility(VISIBLE);
        } else {
            mTopImage.setVisibility(GONE);
        }

        if (mTopImage instanceof HorizontalBannerImageView) {
            float aspectRatio = getBannerImageAspectRatio();
            if (0 != aspectRatio) {
                ((HorizontalBannerImageView) mTopImage).setAspectRatioAndInvalidate(aspectRatio);
            }
        }

        mMainPad.post(new Runnable() {
            @Override
            public void run() {
                // Reset pad size
                reSizeMainPad(mMainPad.getLayoutParams());
                mMainPad.requestLayout();
            }
        });

        setVisibility(GONE);
    }

    protected void setBackground(int drawableId) {
        if (null != mMainPad) {
            mMainPad.setBackgroundResource(drawableId);
        }
    }

    protected void setDialogMarginLeftAndRight(int margin) {
        ViewUtils.setMargins(mMainPad, margin, 0, margin, 0);
    }

    protected void setDialogToBottom() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mMainPad.setLayoutParams(layoutParams);

        int bottomMargin = 0;
        if (CommonUtils.isFloatWindowAllowedBelowNavigationBar()) {
            bottomMargin = CommonUtils.getNavigationBarHeight(getContext());
        }
        ViewUtils.setMargins(mMainPad, 0, 0, 0, bottomMargin);
        ActivityUtils.setNavigationBarColor(mActivity, ContextCompat.getColor(mActivity, android.R.color.black));
        invalidate();
    }

    protected boolean enableTopBackground() {
        return false;
    }

    protected void reSizeMainPad(ViewGroup.LayoutParams layoutParams) {};

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        mWindow = windowManager;
        show();
    }

    @SuppressLint("NewApi")
    protected void show() {
        setVisibility(VISIBLE);
        if (isAppearAlphaAnimation()) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @SuppressLint("NewApi")
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue();
                    @ColorInt int color = (int) mColorEvaluator.evaluate(progress, Color.TRANSPARENT, BACKGROUND_COLOR);
                    mBackground.setBackgroundColor(color);
                    mContainer.setAlpha(progress);
                }
            });

            animator.setDuration(DURATION_ALPHA_IN);
            animator.start();
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
//        if (DeviceEvaluator.getEvaluation() <= DeviceEvaluator.DEVICE_EVALUATION_DEFAULT) {
            mContainer.setAlpha(0f);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @SuppressLint("NewApi")
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue();
                    @ColorInt int color = (int) mColorEvaluator.evaluate(progress, Color.TRANSPARENT, BACKGROUND_COLOR);

                    mBackground.setBackgroundColor(color);

                    mContainer.setAlpha(progress);
                }
            });
            animator.setDuration(DURATION_SLIDE_IN * 2 / 3);
//        } else {
//            mContainer.setAlpha(1f);
//
//            final int screenHeight = CommonUtils.getPhoneHeight(getContext());
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @SuppressLint("NewApi")
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float progress = (float) animation.getAnimatedValue();
//                    @ColorInt int color = (int) mColorEvaluator.evaluate(progress, Color.TRANSPARENT, BACKGROUND_COLOR);
//
//                    mBackground.setBackgroundColor(color);
//
//                    mContainer.setTranslationY(-(1f - progress) * screenHeight);
//                    mContainer.setRotation((1f - progress) * SLIDE_IN_INITIAL_ROTATION_DEGREE);
//                }
//            });
//            animator.setDuration(DURATION_SLIDE_IN);
//        }

        animator.setInterpolator(new AccelerateInterpolator());

        mContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onShown();
                mContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        animator.start();
    }

    protected void onShown() {}

    @Override
    public void dismiss() {
        if (mIsDialogDismissAnimating) {
            return;
        }
        mIsDialogDismissAnimating = true;
        Animator dismissAnimator = getDismissAnimation();
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                FloatWindowManager.getInstance().removeDialog(getType());
                onDismissComplete();
                mIsDialogDismissAnimating = false;
            }
        });
        dismissAnimator.start();

        // Sending a notification (instead of making direct call to LauncherTipManager#notifyDismiss()) to remove
        // dependency on LauncherTipManager class here. This class is placed in common source set.
        HSGlobalNotificationCenter.sendNotification(LauncherConstants.NOTIFICATION_TIP_DISMISS);
    }

    public void dismiss(boolean isPositive) {
        dismiss();
        if (!isPositive) {
            onCanceled();
        }
        HSGlobalNotificationCenter.sendNotification(LauncherConstants.NOTIFICATION_TIP_DISMISS);
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lp.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        }
        this.setLayoutParams(lp);
        return lp;
    }

    @SuppressLint("NewApi")
    protected Animator getDismissAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
//        if (DeviceEvaluator.getEvaluation() <= DeviceEvaluator.DEVICE_EVALUATION_DEFAULT) {

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = (float) animation.getAnimatedValue();
                    @ColorInt int color = (int) mColorEvaluator.evaluate(progress, BACKGROUND_COLOR, Color.TRANSPARENT);

                    // Background color, should be in sync with the navigation bar
                    mBackground.setBackgroundColor(color);

                    mContainer.setAlpha(1f - mDecelerateQuart.getInterpolation(progress));
                }
            });
//        } else {
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float progress = (float) animation.getAnimatedValue();
//                    @ColorInt int color = (int) mColorEvaluator.evaluate(progress, BACKGROUND_COLOR, Color.TRANSPARENT);
//
//                    // Background color, should be in sync with the navigation bar
//                    mBackground.setBackgroundColor(color);
//
//                    mContainer.setTranslationY(progress * mBackground.getHeight());
//                    mContainer.setAlpha(1f - mDecelerateQuart.getInterpolation(progress));
//                }
//            });
//        }
        animator.setDuration(DURATION_ALPHA_OUT);
        animator.setInterpolator(new AccelerateInterpolator(2f));

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        return animator;
    }

    public static class DialogContentContainer extends LinearLayout {
        private FloatingDialog mDialog;

        public DialogContentContainer(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOrientation(VERTICAL);
        }

        private void setDialog(FloatingDialog dialog) {
            mDialog = dialog;
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            mDialog.onDrawBackground(canvas);
            super.dispatchDraw(canvas);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            dismiss(false);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean shouldDismissOnLauncherStop() {
        return true;
    }
}
