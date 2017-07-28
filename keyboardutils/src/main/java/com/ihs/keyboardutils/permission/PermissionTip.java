package com.ihs.keyboardutils.permission;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;


public class PermissionTip extends RelativeLayout {

    public static final int TYPE_TEXT_ACCESSIBILITY = 0;
    public static final int TYPE_TEXT_USAGE = 1;
    public static final int TYPE_TEXT_NOTIFICATION_ACCESS = 2;

    public static final long PERMISSION_TIP_HAND_DURATION = 1000 + 500;
    public static final long PERMISSION_TIP_DISAPPEAR_TIME = 5 * 1000;

    public static int viewWidth;
    public static int viewHeight;

    private WindowManager.LayoutParams mParams;
    private View rootView;
    private ImageView guideTailImageView;
    private ImageView guideHandImageView;
    private ImageView guideCircleImageView;
    private ImageView guideBgImageView;
    private TextView permissionTipTextView;

    private AnimationSet tailAnimationSet;
    private TranslateAnimation fingerAnimation;
    private TranslateAnimation tailTransAnim;
    private ScaleAnimation tailExtendAnim;
    private ScaleAnimation tailShrinkAnim;
    private PermissionAsyncTimer permissionAsyncTimer;

    public PermissionTip(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_permission_tip, this);
        rootView = findViewById(R.id.root_view);
        guideCircleImageView = (ImageView) findViewById(R.id.iv_guide_circle);
        guideTailImageView = (ImageView) findViewById(R.id.iv_guide_tail);
        guideBgImageView = (ImageView) findViewById(R.id.iv_guide_bg);
        permissionTipTextView = (TextView) findViewById(R.id.txt_tip);

        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        rootView.measure(w, h);
        viewHeight = rootView.getMeasuredHeight();
        viewWidth = rootView.getMeasuredWidth();
        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionFloatWindow.getInstance().removePermissionTip();
            }
        });

        guideHandImageView = (ImageView) findViewById(R.id.iv_guide_hand);
        ViewTreeObserver viewTreeObserver = guideCircleImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                startGuideHandAnimation();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    guideCircleImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    guideCircleImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
        setLayoutParams(mParams);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                // 处理自己的逻辑break;
                PermissionFloatWindow.getInstance().removePermissionTip();
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public void startHandAnimationAgain() {
        resetAnimations();
        startGuideHandAnimation();
    }

    public void startGuideHandAnimation() {
        int halfHandWidth = guideHandImageView.getWidth() / 2;
        int toXDelta = guideBgImageView.getWidth() - halfHandWidth;
        float lenMultiple = 40;
        fingerAnimation = new TranslateAnimation(0, toXDelta, 0, 0);
        fingerAnimation.setDuration(PERMISSION_TIP_HAND_DURATION);
        fingerAnimation.setRepeatMode(Animation.RESTART);
        fingerAnimation.setRepeatCount(Animation.INFINITE);

        tailAnimationSet = new AnimationSet(true);
        tailAnimationSet.setInterpolator(fingerAnimation.getInterpolator());

        tailTransAnim = new TranslateAnimation(0, toXDelta, 0, 0);
        tailExtendAnim = new ScaleAnimation(1.0f, lenMultiple, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        tailShrinkAnim = new ScaleAnimation(1.0f, 1 / lenMultiple, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);

        tailAnimationSet.addAnimation(tailShrinkAnim);
        tailAnimationSet.addAnimation(tailExtendAnim);
        tailAnimationSet.addAnimation(tailTransAnim);

        // AnimationSet不能实现无限repeat, 将所有的尾巴动画逐个设置
        tailTransAnim.setDuration(PERMISSION_TIP_HAND_DURATION);
        tailTransAnim.setRepeatMode(Animation.RESTART);
        tailTransAnim.setRepeatCount(Animation.INFINITE);

        tailExtendAnim.setDuration(PERMISSION_TIP_HAND_DURATION);
        tailExtendAnim.setRepeatMode(Animation.RESTART);
        tailExtendAnim.setRepeatCount(Animation.INFINITE);

        tailShrinkAnim.setDuration(PERMISSION_TIP_HAND_DURATION);
        tailShrinkAnim.setRepeatMode(Animation.RESTART);
        tailShrinkAnim.setRepeatCount(Animation.INFINITE);

        guideCircleImageView.startAnimation(fingerAnimation);
        guideHandImageView.startAnimation(fingerAnimation);
        guideTailImageView.startAnimation(tailAnimationSet);

        permissionAsyncTimer = PermissionAsyncTimer.runAsync(new Runnable() {
            @Override
            public void run() {
                PermissionFloatWindow.getInstance().removePermissionTip();
            }
        }, (int) PERMISSION_TIP_DISAPPEAR_TIME);
    }

    public void clean() {
        guideCircleImageView.clearAnimation();
        guideHandImageView.clearAnimation();
        guideTailImageView.clearAnimation();
        if (permissionAsyncTimer != null) {
            permissionAsyncTimer.cancel();
        }
        if (fingerAnimation != null) {
            fingerAnimation.cancel();
        }
        if (tailAnimationSet != null) {
            tailAnimationSet.cancel();
        }
        if (tailExtendAnim != null) {
            tailExtendAnim.cancel();
        }
        if (tailShrinkAnim != null) {
            tailShrinkAnim.cancel();
        }
        if (tailTransAnim != null) {
            tailTransAnim.cancel();
        }
    }

    private void resetAnimations() {
        if (fingerAnimation != null) {
            fingerAnimation.reset();
        }
        if (tailAnimationSet != null) {
            tailAnimationSet.reset();
        }
    }

    public void setPermissionTipText(int tipType) {
        switch (tipType) {
            case TYPE_TEXT_USAGE:

                //"#F9A825"
                String appName = getApplicationName();
                String text = String.format(HSApplication.getContext().getString(R.string.tip_usage_access), appName);
                int index = text.indexOf(appName);
                SpannableStringBuilder styledText = new SpannableStringBuilder(text);
                styledText.setSpan(new ForegroundColorSpan(Color.parseColor("#F9A825")), index, index + appName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                permissionTipTextView.setText(styledText);

                break;
            default:
                break;
        }
    }

    public static String getApplicationName() {
        int stringId = HSApplication.getContext().getApplicationInfo().labelRes;
        String appName = HSApplication.getContext().getString(stringId);
        if (TextUtils.isEmpty(appName)) {
            appName = HSApplication.getContext().getString(R.string.app_name);
        }
        return appName;
    }
}
