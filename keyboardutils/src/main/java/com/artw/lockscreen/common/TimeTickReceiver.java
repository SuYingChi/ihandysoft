package com.artw.lockscreen.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Handler;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

/**
 * Receiving a repeated alarm and check if we need to hint the user to boost the device.
 * Stop when screen off
 */
public class TimeTickReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CLOCK_TIME_CHANGED = "clock_time_changed";

    private static final String TAG = TimeTickReceiver.class.getSimpleName();
    private static TimeTickReceiver sReceiver;
    private static boolean sRegistered;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void register(final Context context) {
        if (sRegistered) {
            HSLog.d(TAG, "Device monitor already registered, skip");
            return;
        }
        sRegistered = true;
        HSLog.d(TAG, "Register time tick on " + context);

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sReceiver = new TimeTickReceiver();
        HSApplication.getContext().registerReceiver(sReceiver, filter);
    }

    public static void unregister() {
        HSApplication.getContext().unregisterReceiver(sReceiver);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        handler.post(()->{
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CLOCK_TIME_CHANGED);
        });
    }


}
