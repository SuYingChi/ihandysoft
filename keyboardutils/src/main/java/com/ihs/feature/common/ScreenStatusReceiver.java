package com.ihs.feature.common;

import android.os.Build;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.RamUsageDisplayUpdater;

public class ScreenStatusReceiver {

    private static final String TAG = ScreenStatusReceiver.class.getSimpleName();
    private static final String WEATHER_TAG = "Weather.Notification";

    public static final String PREF_KEY_WEATHER_NOTIFICATION_TIME = "weather_notification_time";
    public static final String NOTIFICATION_SCREEN_ON = "screen_on";
    public static final String NOTIFICATION_SCREEN_OFF = "screen_off";

    private static boolean sScreenOn = true;
    private static long sScreenOnTime;

    public static boolean isScreenOn() {
        return sScreenOn;
    }

    public static long getScreenOnTime() {
        return sScreenOnTime;
    }

    public static void onScreenOn() {
        HSLog.i(TAG, "Screen on");
        sScreenOn = true;
        sScreenOnTime = System.currentTimeMillis();

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= 16) {
            RamUsageDisplayUpdater.getInstance().startUpdatingRamUsage();
        }

        //// TODO: 17/4/1 打开这里。
//        LChargingScreenUtils.onScreenOn();
    }

    public static void onScreenOff() {
        HSLog.i(TAG, "Screen off");
        sScreenOn = false;
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_OFF);
        LockerChargingScreenUtils.onScreenOff();
        if (Build.VERSION.SDK_INT >= 16) {
            RamUsageDisplayUpdater.getInstance().stopUpdatingRamUsage();
        }
    }

}
