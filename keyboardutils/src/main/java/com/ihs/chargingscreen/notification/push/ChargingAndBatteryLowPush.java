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
import android.widget.TextView;

import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.keyboardutils.R;

/**
 * Created by zhixiangxiao on 5/16/16.
 */
public class ChargingAndBatteryLowPush extends BasePush {

    public static final int TYPE_CHARGING = 0;
    public static final int TYPE_BATTERY_LOW = 1;
    private int pushType;

    private TextView txtTitle;
    private TextView txtBatteryRemainingPercent;
    private TextView txtLeftTimeIndicator;
    private TextView txtChargingLeftTime;

    private ObjectAnimator disappearAnimator;

    @IntDef({TYPE_CHARGING, TYPE_BATTERY_LOW})
    public @interface PushType {
    }

    @TargetApi(11)
    public ChargingAndBatteryLowPush(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public ChargingAndBatteryLowPush(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChargingAndBatteryLowPush(Context context, @PushType int pushType) {
        super(context);

        this.pushType = pushType;

        txtTitle = (TextView) rootView.findViewById(R.id.txt_title);
        txtBatteryRemainingPercent = (TextView) rootView.findViewById(R.id.txt_battery_remaining_percent);
        txtLeftTimeIndicator = (TextView) rootView.findViewById(R.id.txt_left_time_indicator);
        txtChargingLeftTime = (TextView) rootView.findViewById(R.id.txt_charging_left_time);

        updatePush();

//        if (pushType == TYPE_CHARGING) {
//            setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
//                        enableChargingAndStartActivity();
//                    }
//
//                }
//            });
//        }

    }



    public void updatePush() {
        for (int i = 0; i < imgBatteryList.size(); i++) {
            if (i < ChargingManagerUtil.getImgBatteryVisibleCount()) {
                imgBatteryList.get(i).setVisibility(VISIBLE);
            } else {
                imgBatteryList.get(i).setVisibility(INVISIBLE);
            }
        }

        if (pushType == TYPE_CHARGING) {
            txtTitle.setText(getContext().getResources().getString(R.string.charging_module_fast_charging));
            txtBatteryRemainingPercent.setText(String.valueOf(HSChargingManager.getInstance().getBatteryRemainingPercent()));
            txtLeftTimeIndicator.setVisibility(VISIBLE);
            txtLeftTimeIndicator.setText(ChargingManagerUtil.getLeftTimeIndicatorString());
            txtChargingLeftTime.setVisibility(VISIBLE);
            txtChargingLeftTime.setText(ChargingManagerUtil.getChargingLeftTimeString(HSChargingManager.getInstance().getChargingLeftMinutes()));

        } else {
            txtTitle.setText(getContext().getResources().getString(R.string.charging_module_battery_low));
            txtBatteryRemainingPercent.setText(String.valueOf(HSChargingManager.getInstance().getBatteryRemainingPercent()));
            txtLeftTimeIndicator.setVisibility(GONE);
            txtChargingLeftTime.setVisibility(GONE);
        }
    }

    @Override
    public boolean show() {
        return super.show(false);
    }


    public int getRootViewLayoutId() {
        return R.layout.charging_module_push_charging;
    }

    public void initLayoutParamsGravityAndType() {
//        if (pushType == TYPE_CHARGING) {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        } else {
//            layoutParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
//        }
        layoutParams.gravity = Gravity.TOP;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    protected void startPushAppearAnimation() {
        PropertyValuesHolder appearAlpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 1.0f);
        PropertyValuesHolder appearTransY = PropertyValuesHolder.ofFloat("translationY", -rootView.getMeasuredHeight(), 0);
        appearAnimator = ObjectAnimator.ofPropertyValuesHolder(this, appearAlpha, appearTransY);
        appearAnimator.setDuration(PUSH_APPEAR_DURATION);

        PropertyValuesHolder disappearAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.3f);
        PropertyValuesHolder disappearTransY = PropertyValuesHolder.ofFloat("translationY", 0, -rootView.getMeasuredHeight());
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
