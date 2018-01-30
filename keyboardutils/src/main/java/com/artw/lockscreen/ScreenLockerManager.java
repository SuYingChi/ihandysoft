package com.artw.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.common.TimeTickReceiver;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.chargingscreen.utils.LockerChargingSpecialConfig;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.kc.utils.KCAnalytics;

import net.appcloudbox.ads.expressads.AcbExpressAdManager;


/**
 * Created by Arthur on 17/3/31.
 */

public class ScreenLockerManager {

    private static String lockerAdPlacement;
    private static String resultPageAdPlacement;

    public static String getLockerAdPlacement() {
        return lockerAdPlacement;
    }

    public static String getResultPageAdPlacement() {
        return resultPageAdPlacement;
    }

    public static void init(String resultPageAdPlacement, String lockerAdPlacement) {
        ScreenLockerManager.resultPageAdPlacement = resultPageAdPlacement;
        ScreenLockerManager.lockerAdPlacement = lockerAdPlacement;

        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);

        WebContentSearchManager.getInstance();
        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Runnable runnable = ()->{
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                        ScreenStatusReceiver.onScreenOff();
                    } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        ScreenStatusReceiver.onScreenOn();
                    }
                };

                if (HSConfig.optBoolean(false, "Application", "DisableScreenBroadcastDelay")) {
                    runnable.run();
                } else {
                    new Handler().post(runnable);
                }

            }
        }, screenFilter);

        LockerSettings.initLockerState();
        TimeTickReceiver.register(HSApplication.getContext());

        INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (s.equals(ChargingScreenActivity.NOTIFICATION_CHARGING_ACTIVITY_STARTED)) {
                    LockerChargingScreenUtils.finishLockerActivity();
                } else if (s.equals(ChargingFullScreenAlertDialogActivity.NOTIFICATION_LOCKER_ENABLED)) {
                    enableLockerFromAlert();
                } else if (s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)) {
                    LockerSettings.updateLockerSetting();
                }
            }
        };
        HSGlobalNotificationCenter.addObserver(ChargingScreenActivity.NOTIFICATION_CHARGING_ACTIVITY_STARTED, observer);
        HSGlobalNotificationCenter.addObserver(ChargingFullScreenAlertDialogActivity.NOTIFICATION_LOCKER_ENABLED, observer);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, observer);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, observer);
        if (LockerSettings.isLockerEnabled() && !RemoveAdsManager.getInstance().isRemoveAdsPurchased()
                && LockerChargingSpecialConfig.getInstance().shouldShowAd()) {
            AcbExpressAdManager.getInstance().activePlacementInProcess(lockerAdPlacement);
        }
    }

    public static void enableLockerFromAlert() {
        LockerSettings.setLockerEnabled(true);
        Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.screen_locker_enable_alert_toast), Toast.LENGTH_SHORT).show();
        KCAnalytics.logEvent("alert_screen_locker_click");
    }
}
