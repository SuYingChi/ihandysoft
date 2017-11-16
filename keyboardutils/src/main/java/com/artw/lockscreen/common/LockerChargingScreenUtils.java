package com.artw.lockscreen.common;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.AndroidRuntimeException;

import com.artw.lockscreen.LockerActivity;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.chargingscreen.utils.FeatureDelayReleaseUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.launcher.FloatWindowController;

public class LockerChargingScreenUtils {

    private static final long MIN_INTERVAL_VALID_CLICK = 500;
    private static final String PREF_APP_FIRST_TRY_TO_LOCKER = "pref_app_first_try_to_locker";

    private static volatile long lastClickTime;

    public static boolean isFastDoubleClick() {

        long currentClickTime = SystemClock.elapsedRealtime();
        long intervalClick = currentClickTime - lastClickTime;

        if (0 < intervalClick && intervalClick < MIN_INTERVAL_VALID_CLICK) {
            return true;
        }

        lastClickTime = currentClickTime;
        return false;
    }

    public static void onScreenOn() {
        if (HSChargingManager.getInstance().isCharging()) {//&& ChargingScreenSettings.isChargingScreenEnabled()) {
            finishLockerActivity();
        } else if (LockerSettings.isLockerEnabled()) {
            startLockerActivity();
        }
    }

    public static void onScreenOff() {
        if (HSChargingManager.getInstance().isCharging() && ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE) {
            finishLockerActivity();
        } else if (LockerSettings.isLockerEnabled() && !shouldBlockLockerForNewUser()) {
            startLockerActivity();
        }
    }

    // 对于DEFAULT_ACTIVE的模式，可以配置一个时间差让新用户看不到此功能；其它模式不支持时间差
    private static boolean shouldBlockLockerForNewUser() {
        if (LockerSettings.getLockerEnableStates() == LockerSettings.LOCKER_DEFAULT_ACTIVE) {
            int delayHours = HSConfig.optInteger(0, "Application", "Locker", "HoursFromFirstUse");
            return !FeatureDelayReleaseUtil.checkFeatureReadyToWork(PREF_APP_FIRST_TRY_TO_LOCKER, delayHours);
        } else {
            return false;
        }
    }

    private static boolean isReady(String key, int delayHours) {
        boolean lockerReadyToWork = FeatureDelayReleaseUtil.checkFeatureReadyToWork(key, delayHours);
        if (LockerSettings.isLockerEnabledBefore()) {
            return true;
        }
        int moduleStates = LockerSettings.getLockerEnableStates();
        return moduleStates != LockerSettings.LOCKER_DEFAULT_ACTIVE || lockerReadyToWork;
    }

    public static void finishLockerActivity() {
        if (isCalling()) {
            return;
        }

        //todo complete charging
        HSGlobalNotificationCenter.sendNotification(LockerActivity.EVENT_FINISH_SELF);
    }

    public static void startLockerActivity() {
        if (isCalling()) {
            return;
        }

        if (!HSConfig.optBoolean(false, "Application", "Locker", "UseNewLockScreen")) {
            HSLog.d("config use past screen locker");
            try {
                Intent intent = new Intent(HSApplication.getContext(), LockerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                HSApplication.getContext().startActivity(intent);
            } catch (AndroidRuntimeException e) {
                e.printStackTrace();
            }
        } else {
            HSLog.d("config use new screen locker");
            FloatWindowController.getInstance().showLockScreen();
        }
    }

    public static boolean isCalling() {
        TelephonyManager telephonyMgr = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }

    /**
     * 原生5.0.x系统上，系统锁屏和同时设置了FLAG_FULLSCREEN、FLAG_SHOW_WHEN_LOCKED的Activity闪烁冲突,
     * 初步判断是5.0.x上WMS在处理Window优先级上存在bug。为避免冲突，取消冲突Activity的FLAG_FULLSCREEN。
     * <p>
     * 该方法判断是否为原生5.0.x系统
     *
     * @return is native 5.0.x
     */
    public static boolean isNativeLollipop() {
        return Build.VERSION_CODES.LOLLIPOP == Build.VERSION.SDK_INT
                && ("Google".equals(Build.BRAND) || "google".equals(Build.BRAND));
    }
}