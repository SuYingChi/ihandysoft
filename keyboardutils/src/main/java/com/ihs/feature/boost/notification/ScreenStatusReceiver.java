package com.ihs.feature.boost.notification;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.RamUsageDisplayUpdater;

public class ScreenStatusReceiver {

    private static final String TAG = ScreenStatusReceiver.class.getSimpleName();

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
        RamUsageDisplayUpdater.getInstance().startUpdatingRamUsage();
    }

    public static void onScreenOff() {
        HSLog.i(TAG, "Screen off");
        sScreenOn = false;
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SCREEN_OFF);
        RamUsageDisplayUpdater.getInstance().stopUpdatingRamUsage();
    }
}
