package com.artw.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.common.ScreenStatusReceiver;
import com.artw.lockscreen.common.TimeTickReceiver;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.keyboardutils.R;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;

/**
 * Created by Arthur on 17/3/31.
 */

public class ScreenLockerManager {


    public static void init() {

        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn();
                }
            }
        }, screenFilter);

        LockerSettings.initLockerState();
        TimeTickReceiver.register(HSApplication.getContext());

        INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if(s.equals(ChargingScreenActivity.NOTIFICATION_CHARGING_ACTIVITY_STARTED)){
                    LockerChargingScreenUtils.finishLockerActivity();
                }else if(s.equals(ChargingFullScreenAlertDialogActivity.NOTIFICATION_LOCKER_ENABLED)){
                    enableLockerFromAlert();
                }else if(s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)){
                    LockerSettings.setLockerEnabled(LockerSettings.isLockerEnabled(),"plist");
                }else if(s.equals(HSNotificationConstant.HS_SESSION_END)){
                    LockerSettings.setLockerForFirstSession();
                }
            }
        };
        HSGlobalNotificationCenter.addObserver(ChargingScreenActivity.NOTIFICATION_CHARGING_ACTIVITY_STARTED, observer);
        HSGlobalNotificationCenter.addObserver(ChargingFullScreenAlertDialogActivity.NOTIFICATION_LOCKER_ENABLED, observer);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, observer);
    }

    public static void enableLockerFromAlert() {
        LockerSettings.setLockerEnabled(true,"alert");
        Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.screen_locker_enable_alert_toast), Toast.LENGTH_SHORT).show();
        HSAnalytics.logEvent("alert_screen_locker_click");
    }
}
