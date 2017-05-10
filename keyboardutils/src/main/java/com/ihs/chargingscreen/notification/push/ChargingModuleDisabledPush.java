package com.ihs.chargingscreen.notification.push;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.ihs.chargingscreen.ui.RippleDrawableUtils;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.keyboardutils.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.ihs.chargingscreen.notification.ChargingNotificationManager.getRemoteViewsTitle;

/**
 * Created by Arthur on 2017/2/27
 * <p>
 * push when charging module disabled.
 */
public class ChargingModuleDisabledPush extends BasePush {

    public static final int TYPE_FULL_CHARGED = 0;
    public static final int TYPE_CHARGING_PLUG = 1;
    private int pushType;

    private TextView txtTitle;
    private TextView txtLeftTimeIndicator;

    private ObjectAnimator disappearAnimator;

    @IntDef({TYPE_FULL_CHARGED, TYPE_CHARGING_PLUG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PushType {
    }

    @TargetApi(11)
    public ChargingModuleDisabledPush(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public ChargingModuleDisabledPush(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChargingModuleDisabledPush(Context context, @PushType int pushType) {
        super(context);

        this.pushType = pushType;

        txtTitle = (TextView) rootView.findViewById(R.id.txt_title);
        txtLeftTimeIndicator = (TextView) rootView.findViewById(R.id.txt_left_time_indicator);

        View enableTv = rootView.findViewById(R.id.tv_enable);
        enableTv.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.charging_green));
        enableTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChargingManagerUtil.enableCharging(true,"popUp");
                ChargingAnalytics.getInstance().chargingEnableNotificationClicked();
            }
        });
        updatePush();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChargingManagerUtil.enableCharging(true,"popUp");
                ChargingAnalytics.getInstance().chargingEnableNotificationClicked();
            }
        });

    }

    public void updatePush(int type) {
        this.pushType = type;
        updatePush();
    }

    public void updatePush() {

        String remoteViewsTitle = getRemoteViewsTitle();

        if (pushType == TYPE_CHARGING_PLUG) {
            txtTitle.setText(remoteViewsTitle);

        } else if (pushType == TYPE_FULL_CHARGED) {
            txtTitle.setText(getContext().getResources().getString(R.string.charging_module_real_full_charged));
        }

        txtLeftTimeIndicator.setVisibility(VISIBLE);
        txtLeftTimeIndicator.setText(R.string.enable_charging_detail);

    }

    @Override
    public boolean show() {
        return super.show(false);
    }


    public int getRootViewLayoutId() {
        return R.layout.charging_module_disabled_plug;
    }

    public void initLayoutParamsGravityAndType() {
        layoutParams.gravity = Gravity.BOTTOM;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    protected void startPushAppearAnimation() {

        PropertyValuesHolder appearAlpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 1.0f);
        PropertyValuesHolder appearTransY = PropertyValuesHolder.ofFloat("translationY", rootView.getMeasuredHeight(), 0);
        appearAnimator = ObjectAnimator.ofPropertyValuesHolder(this, appearAlpha, appearTransY);
        appearAnimator.setDuration(PUSH_APPEAR_DURATION);

        PropertyValuesHolder disappearAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.3f);
        PropertyValuesHolder disappearTransY = PropertyValuesHolder.ofFloat("translationY", 0, rootView.getMeasuredHeight());
        disappearAnimator = ObjectAnimator.ofPropertyValuesHolder(this, disappearAlpha, disappearTransY);
        disappearAnimator.setStartDelay(PUSH_DURATION);
        disappearAnimator.setDuration(PUSH_APPEAR_DURATION);

        appearAnimator.start();
        disappearAnimator.start();
        disappearAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hide();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    protected void cancelAnimation() {

        if (appearAnimator != null) {
            appearAnimator.removeAllListeners();
            appearAnimator.cancel();
            appearAnimator = null;
        }

        if (disappearAnimator != null) {
            disappearAnimator.removeAllListeners();
            disappearAnimator.cancel();
            disappearAnimator = null;
        }
    }

}
