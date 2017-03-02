package com.ihs.chargingscreen.notification.push;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhixiangxiao on 5/16/16.
 */
public class WarningPush extends BasePush {

    public static final int WARNING_LOW_VOLTAGE = 0;
    public static final int WARNING_BATTERY_DOCTOR = 1;
    public static final int WARNING_CUT_OFF_CHARGE = 2;
    public static final int WARNING_ENABLE_PLUG = 3;

    public static final String WARNING_ICON = "WARNING_ICON";
    public static final String WARNING_TITLE = "WARNING_TITLE";
    public static final String WARNING_TIP = "WARNING_TIP";

    private int pushType;

    private ValueAnimator imgBatteryDisappearAnimator;
    private List<ValueAnimator> imgBatteryAppearAnimators = new ArrayList<>();


    @IntDef({WARNING_LOW_VOLTAGE, WARNING_BATTERY_DOCTOR, WARNING_CUT_OFF_CHARGE, WARNING_ENABLE_PLUG})
    public @interface WarningType {
    }

    @TargetApi(11)
    public WarningPush(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public WarningPush(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WarningPush(Context context, @WarningType final int pushType) {
        super(context);

        this.pushType = pushType;

        Map<String, Integer> warningInfoResIds = getWarningInfo(pushType);

        ImageView imgWarningSign = (ImageView) rootView.findViewById(R.id.img_warning_sign);
        imgWarningSign.setBackgroundResource(warningInfoResIds.get(WARNING_ICON));

        TextView txtWarningTitle = (TextView) rootView.findViewById(R.id.txt_warning_title);
        txtWarningTitle.setText(context.getResources().getString(warningInfoResIds.get(WARNING_TITLE)));

        TextView txtWarningTip = (TextView) rootView.findViewById(R.id.txt_warning_tip);
        txtWarningTip.setText(context.getResources().getString(warningInfoResIds.get(WARNING_TIP)));

        updatePush();

    }

    public int getRootViewLayoutId() {
        return R.layout.charging_module_push_warning;
    }

    public void initLayoutParamsGravityAndType() {
//        layoutParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        layoutParams.gravity = Gravity.BOTTOM;
    }

    public void updatePush() {
        for (int i = 0; i < imgBatteryList.size(); i++) {
            if (i < ChargingManagerUtil.getImgBatteryVisibleCount()) {
                imgBatteryList.get(i).setVisibility(VISIBLE);
            } else {
                imgBatteryList.get(i).setVisibility(INVISIBLE);
            }
        }

        if (pushType != WARNING_CUT_OFF_CHARGE) {
            startImgBatteryAnimators(ChargingManagerUtil.getImgBatteryVisibleCount());
        }
    }

    private void startImgBatteryAnimators(int imgBatteryVisibleCount) {
        imgBatteryAppearAnimators = new ArrayList<>();

        for (int i = 0; i < imgBatteryVisibleCount; i++) {

            ValueAnimator valueAnimator = ObjectAnimator.ofFloat(imgBatteryList.get(i), "alpha", 0.7f, 1f);
            valueAnimator.setDuration(300);
            valueAnimator.setStartDelay(180 * i + 250);

            imgBatteryAppearAnimators.add(valueAnimator);
        }

        imgBatteryAppearAnimators.get(imgBatteryVisibleCount - 1).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (imgBatteryDisappearAnimator != null) {
                    imgBatteryDisappearAnimator.start();
                }
            }
        });


        Property<WarningPush, Float> imgBatteryAlphaProperty = new Property<WarningPush, Float>(Float.class, "imgBatteryAlpha") {
            @Override
            public Float get(WarningPush object) {
                return imgBatteryList.get(0).getAlpha();
            }

            @Override
            public void set(WarningPush object, Float value) {
                for (ImageView imgBattery : imgBatteryList) {
                    imgBattery.setAlpha(value);
                }
            }
        };

        imgBatteryDisappearAnimator = ObjectAnimator.ofFloat(this, imgBatteryAlphaProperty, 1f, 0.7f);
        imgBatteryDisappearAnimator.setDuration(500);
        imgBatteryDisappearAnimator.setStartDelay(150);
        imgBatteryDisappearAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                for (ValueAnimator valueAnimator : imgBatteryAppearAnimators) {
                    valueAnimator.start();
                }
            }
        });

        imgBatteryDisappearAnimator.start();
    }


    protected void startPushAppearAnimation() {
        PropertyValuesHolder appearAlpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 1.0f);
        PropertyValuesHolder appearTransY = PropertyValuesHolder.ofFloat("translationY", rootView.getMeasuredHeight(), 0);
        appearAnimator = ObjectAnimator.ofPropertyValuesHolder(this, appearAlpha, appearTransY);
        appearAnimator.setDuration(PUSH_APPEAR_DURATION);
        appearAnimator.start();
    }

    protected void cancelAnimation() {

        if (appearAnimator != null) {
            appearAnimator.removeAllListeners();
            appearAnimator.cancel();
            appearAnimator = null;
        }

        if (imgBatteryDisappearAnimator != null) {
            imgBatteryDisappearAnimator.removeAllListeners();
            imgBatteryDisappearAnimator.cancel();
            imgBatteryDisappearAnimator = null;
        }

        if (imgBatteryAppearAnimators != null) {
            for (ValueAnimator valueAnimator : imgBatteryAppearAnimators) {
                if (valueAnimator != null) {
                    valueAnimator.removeAllListeners();
                    valueAnimator.cancel();
                }
            }
            imgBatteryAppearAnimators = null;
        }
    }


    private Map<String, Integer> getWarningInfo(@WarningType int warningType) {

        Map<String, Integer> warningInfoResId = new HashMap<>();

        switch (warningType) {
            case WARNING_LOW_VOLTAGE:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_push_lowvoltage);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_low_voltage);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_low_voltage_tip);
                break;

            case WARNING_BATTERY_DOCTOR:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_push_battery_tips);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_battery_doctor);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_battery_doctor_tip);
                break;

            case WARNING_CUT_OFF_CHARGE:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_push_cutoff_charger);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_cut_off_charger);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_cut_off_charger_tip);
                break;

            case WARNING_ENABLE_PLUG:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_push_battery_tips);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_enable_plug_title);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_enable_plug_tip);
                break;

            default:

                warningInfoResId.put(WARNING_ICON, -1);
                warningInfoResId.put(WARNING_TITLE, -1);
                warningInfoResId.put(WARNING_TIP, -1);
                break;
        }

        return warningInfoResId;
    }


}
