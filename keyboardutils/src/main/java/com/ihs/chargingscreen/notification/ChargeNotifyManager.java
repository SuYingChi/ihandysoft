package com.ihs.chargingscreen.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.IntDef;

import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.Constants;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.notification.push.BasePush;
import com.ihs.chargingscreen.notification.push.ChargingAndBatteryLowPush;
import com.ihs.chargingscreen.notification.push.ChargingModuleDisabledPush;
import com.ihs.chargingscreen.notification.push.FullChargedPush;
import com.ihs.chargingscreen.notification.push.WarningPush;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.chargingscreen.utils.LockerChargingSpecialConfig;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.kc.utils.FeatureDelayReleaseUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhixiangxiao on 5/19/16.
 */
public class ChargeNotifyManager {

    private static final int EVENT_SHOW_LOW_VOLTAGE_PUSH = 1111;
    private static final int EVENT_SHOULD_NOT_SHOW_PUSH = 1112;

    private static final int INTERVAL_TIME_OF_SHOW_PUSH_LOW_VOLTAGE = 10 * 60 * 1000;
    private static final int PUSH_FULL_CHARGED_MAX_SHOWED_COUNT = 5;
    private static final String PREF_APP_FIRST_TRY_TO_CHARGING = "pref_app_first_try_to_charging";

    public static final int PUSH_FULL_CHARGE_PRIORITY = 0x20;
    public static final int PUSH_BATTERY_DOCTOR_PRIORITY = 0x10;
    public static final int PUSH_CUT_OFF_CHARGE_PRIORITY = 0x08;
    public static final int PUSH_CHARGING_PRIORITY = 0x04;
    public static final int PUSH_BATTERY_LOW_PRIORITY = 0x02;
    public static final int PUSH_LOW_VOLTAGE_PRIORITY = 0x01;

    public static final int PUSH_ENABLE_WHEN_PLUG = 0x40;
    public static final int PUSH_ENABLE_WHEN_FULL_CHARGE = 0x80;

    private static ChargeNotifyManager instance;

    private WarningPush lowVoltagePush;
    private WarningPush batteryDoctorPush;
    private WarningPush cutoffChargePush;

    private FullChargedPush fullChargedPush;

    private ChargingAndBatteryLowPush chargingPush;
    private ChargingAndBatteryLowPush batteryLowPush;
    private ChargingModuleDisabledPush disabledPush;


    private ChargingNotificationManager chargingNotification;

    private PowerManager powerManager;

    private volatile int showPushType = 0;

    private boolean isChargingActivityAlive;

    @IntDef({PUSH_FULL_CHARGE_PRIORITY, PUSH_BATTERY_DOCTOR_PRIORITY, PUSH_CUT_OFF_CHARGE_PRIORITY,
            PUSH_CHARGING_PRIORITY, PUSH_BATTERY_LOW_PRIORITY, PUSH_LOW_VOLTAGE_PRIORITY, PUSH_ENABLE_WHEN_PLUG, PUSH_ENABLE_WHEN_FULL_CHARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PushType {
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case EVENT_SHOW_LOW_VOLTAGE_PUSH:
                    showLowVoltagePush();
                    break;

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!HSChargingScreenManager.getInstance().isChargingModuleOpened() ||
                    ChargingPrefsUtil.getChargingEnableStates() != ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE
                    ) {
                return;
            }

            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                int delayHours = HSConfig.optInteger(0, "Application", "ChargeLocker", "HoursFromFirstUse");
                boolean chargingReadyToWork = isReady(PREF_APP_FIRST_TRY_TO_CHARGING, delayHours);
                LockerChargingSpecialConfig.getInstance().rebindService();
                if (HSChargingManager.getInstance().isCharging() && chargingReadyToWork) {
                    ChargingManagerUtil.startChargingActivity();
                }
            }
        }
    };

    private boolean isReady(String key, int delayHours) {
        boolean chargingReadyToWork = FeatureDelayReleaseUtils.isFeatureAvailable(HSApplication.getContext(), key, delayHours);
        if (ChargingPrefsUtil.getInstance().isChargingEnableByUser()) {
            return true;
        }
        int moduleStates = ChargingPrefsUtil.getChargingEnableStates();
        return moduleStates != ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE || chargingReadyToWork;
    }

    private ChargeNotifyManager() {

        powerManager = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);

        HSGlobalNotificationCenter.addObserver(Constants.EVENT_CHARGING_SHOW_PUSH, new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                showPush();
            }
        });
    }

    public static synchronized ChargeNotifyManager getInstance() {
        if (instance == null) {
            instance = new ChargeNotifyManager();
        }
        return instance;
    }

    public void registerScreenOffReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        HSApplication.getContext().registerReceiver(receiver, intentFilter);
    }

    public void unregisterScreenOffReceiver() {
        //预防特殊情况没有注册却unregister
        try {
            HSApplication.getContext().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsChargingActivityAlive(boolean isAlive) {
        isChargingActivityAlive = isAlive;
    }

    public void pendingToShow(@PushType int pushType) {
        showPushType += pushType;
    }

    private void showPush() {

        HSLog.e("showPush: " + showPushType);

        /**根据需求去掉之前所有push*/
        if (showPushType == 0 || showPushType < PUSH_ENABLE_WHEN_PLUG) {
            return;
        }

        if (!ChargingManagerUtil.isPushEnabled()) {
            showPushType = 0;
            return;
        }

        if (handler.hasMessages(EVENT_SHOULD_NOT_SHOW_PUSH)) {
            showPushType = 0;
            return;
        }

        if (isChargingActivityAlive) {
            showPushType = 0;
            return;
        }

        if (!HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
            if ((showPushType & PUSH_ENABLE_WHEN_PLUG) != 0) {
                showEnableWhenPlug();
            }

            if ((showPushType & PUSH_ENABLE_WHEN_FULL_CHARGE) != 0) {
                showEnableWhenFullCharge();
            }
            showPushType = 0;
            return;
        }

        if ((showPushType & PUSH_FULL_CHARGE_PRIORITY) != 0) {
            showFullChargedPush();

            MediaPlayer player = new MediaPlayer().create(HSApplication.getContext(), R.raw.charging_module_full_charged_voice);
            player.start();
        } else if ((showPushType & PUSH_BATTERY_DOCTOR_PRIORITY) != 0) {

            showBatteryDoctorPush();
        } else if ((showPushType & PUSH_CUT_OFF_CHARGE_PRIORITY) != 0) {

            showCutoffChargePush();
        } else if ((showPushType & PUSH_CHARGING_PRIORITY) != 0) {

            showChargingPush();

            if ((showPushType & PUSH_LOW_VOLTAGE_PRIORITY) != 0) {
                handler.removeMessages(EVENT_SHOW_LOW_VOLTAGE_PUSH);
                handler.sendEmptyMessageDelayed(EVENT_SHOW_LOW_VOLTAGE_PUSH, INTERVAL_TIME_OF_SHOW_PUSH_LOW_VOLTAGE);
            }

        } else if ((showPushType & PUSH_BATTERY_LOW_PRIORITY) != 0) {

            showBatteryLowPush();
        } else if ((showPushType & PUSH_LOW_VOLTAGE_PRIORITY) != 0) {

            showLowVoltagePush();
            handler.removeMessages(EVENT_SHOW_LOW_VOLTAGE_PUSH);
            handler.sendEmptyMessageDelayed(EVENT_SHOW_LOW_VOLTAGE_PUSH, INTERVAL_TIME_OF_SHOW_PUSH_LOW_VOLTAGE);
        }

        handler.sendEmptyMessageDelayed(EVENT_SHOULD_NOT_SHOW_PUSH,
                BasePush.PUSH_APPEAR_OFFSET + BasePush.PUSH_APPEAR_DURATION + BasePush.PUSH_DURATION);

        showPushType = 0;
    }

    private void showLowVoltagePush() {

        if (lowVoltagePush == null) {
            lowVoltagePush = new WarningPush(HSApplication.getContext(), WarningPush.WARNING_LOW_VOLTAGE);
        } else {
            lowVoltagePush.updatePush();
        }
        lowVoltagePush.show();
    }

    public void cancelLowVoltagePush() {
        handler.removeMessages(EVENT_SHOW_LOW_VOLTAGE_PUSH);
    }

    private void showBatteryDoctorPush() {

        if (batteryDoctorPush == null) {
            batteryDoctorPush = new WarningPush(HSApplication.getContext(), WarningPush.WARNING_BATTERY_DOCTOR);
        } else {
            batteryDoctorPush.updatePush();
        }
        batteryDoctorPush.show();
    }

    private void showEnableWhenPlug() {

        if (disabledPush == null) {
            disabledPush = new ChargingModuleDisabledPush(HSApplication.getContext(), ChargingModuleDisabledPush.TYPE_CHARGING_PLUG);
        } else {
            disabledPush.updatePush(ChargingModuleDisabledPush.TYPE_CHARGING_PLUG);
        }
        disabledPush.show();
    }

    private void showEnableWhenFullCharge() {

        if (disabledPush == null) {
            disabledPush = new ChargingModuleDisabledPush(HSApplication.getContext(), ChargingModuleDisabledPush.TYPE_FULL_CHARGED);
        } else {
            disabledPush.updatePush(ChargingModuleDisabledPush.TYPE_FULL_CHARGED);
        }
        disabledPush.show();

    }

    private void showCutoffChargePush() {

        if (cutoffChargePush == null) {
            cutoffChargePush = new WarningPush(HSApplication.getContext(), WarningPush.WARNING_CUT_OFF_CHARGE);
        } else {
            cutoffChargePush.updatePush();
        }
        cutoffChargePush.show();
    }

    private void showFullChargedPush() {

        if (ChargingPrefsUtil.getFullChargedPushShowedCount() >= PUSH_FULL_CHARGED_MAX_SHOWED_COUNT) {
            return;
        }

        if (fullChargedPush == null) {
            fullChargedPush = new FullChargedPush(HSApplication.getContext());
        } else {
            fullChargedPush.updatePush();
        }
        fullChargedPush.show();

        ChargingPrefsUtil.increaseFullChargedPushShowedCount();
    }

    private void showChargingPush() {

        if (!powerManager.isScreenOn()) {
            return;
        }
        if (chargingPush == null) {
            chargingPush = new ChargingAndBatteryLowPush(HSApplication.getContext(), ChargingAndBatteryLowPush.TYPE_CHARGING);
        } else {
            chargingPush.updatePush();
        }
        chargingPush.show();

    }

    private void showBatteryLowPush() {

        if (batteryLowPush == null) {
            batteryLowPush = new ChargingAndBatteryLowPush(HSApplication.getContext(), ChargingAndBatteryLowPush.TYPE_BATTERY_LOW);
        } else {
            batteryLowPush.updatePush();
        }
        batteryLowPush.show();
    }

    public void refreshChargingNotification() {

        HSLog.i("refreshChargingNotification");
        HSLog.i("isCharging: " + HSChargingManager.getInstance().isCharging());
        HSLog.i("isChargingModuleOpened: " + HSChargingScreenManager.getInstance().isChargingModuleOpened());

//        if (!HSChargingManager.getInstance().isCharging() ||
//                !HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
//            if (chargingNotification != null) {
//                chargingNotification.cancel();
//                chargingNotification = null;
//            }
//            return;
//        }

        if (chargingNotification == null) {
            chargingNotification = new ChargingNotificationManager();
        }

        if (!HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
            chargingNotification.update();
        }
    }

    public void removeNotification() {
        if (chargingNotification != null) {
            chargingNotification.cancel();
        }
    }

}
