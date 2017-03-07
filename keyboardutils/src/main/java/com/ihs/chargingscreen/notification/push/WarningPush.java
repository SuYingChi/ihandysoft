package com.ihs.chargingscreen.notification.push;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.keyboardutils.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
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

    @IntDef({WARNING_LOW_VOLTAGE, WARNING_BATTERY_DOCTOR, WARNING_CUT_OFF_CHARGE, WARNING_ENABLE_PLUG})
    @Retention(RetentionPolicy.SOURCE)
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

        if (pushType != WARNING_CUT_OFF_CHARGE) {
            startImgBatteryAnimators(ChargingManagerUtil.getImgBatteryVisibleCount());
        }
    }

    private void startImgBatteryAnimators(int imgBatteryVisibleCount) {
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

    }


    private Map<String, Integer> getWarningInfo(@WarningType int warningType) {

        Map<String, Integer> warningInfoResId = new HashMap<>();

        switch (warningType) {
            case WARNING_LOW_VOLTAGE:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_notify_charging_small_icon);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_low_voltage);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_low_voltage_tip);
                break;

            case WARNING_BATTERY_DOCTOR:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_notify_charging_small_icon);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_battery_doctor);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_battery_doctor_tip);
                break;

            case WARNING_CUT_OFF_CHARGE:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_notify_charging_small_icon);
                warningInfoResId.put(WARNING_TITLE, R.string.charging_module_cut_off_charger);
                warningInfoResId.put(WARNING_TIP, R.string.charging_module_cut_off_charger_tip);
                break;

            case WARNING_ENABLE_PLUG:

                warningInfoResId.put(WARNING_ICON, R.mipmap.charging_module_notify_charging_small_icon);
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
