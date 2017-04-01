package com.ihs.chargingscreen.utils;

import android.content.Context;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 17/3/2.
 */

public class ChargingGARecorder {

    private static ChargingGARecorder instance = null;
    private static Context context = HSApplication.getContext();

    private String NativeAd_Charging_Load;
    private String NativeAd_Charging_Show;
    private String NativeAd_Charging_Click;

    private ChargingGARecorder() {
        NativeAd_Charging_Load = "NativeAd_" + context.getPackageName() + "_Charging_Load";
        NativeAd_Charging_Show = "NativeAd_" + context.getPackageName() + "_Charging_Show";
        NativeAd_Charging_Click = "NativeAd_" + context.getPackageName() + "_Charging_Click";
    }

    public static synchronized ChargingGARecorder getInstance() {
        if (instance == null) {
            instance = new ChargingGARecorder();
        }
        return instance;
    }


    static final String app_chargingLocker_enable = "app_chargingLocker_enable";//充电锁屏开启 - 每个用户只记一次
    private static final String app_chargingLocker_show = "app_chargingLocker_show";//充电锁屏展示
    private static final String notification_chargingLocker_show = "notification_chargingLocker_show";//充电锁屏通知出现
    private static final String notification_chargingLocker_click = "notification_chargingLocker_click";//充电锁屏通知点击
    private static final String app_chargingLocker_disable_clicked = "app_chargingLocker_disable_clicked";//关闭按钮点击  - 每个用户只记一次
    private static final String app_chargingLocker_disable = "app_chargingLocker_disable";//充电锁屏关闭 - 每个用户只记一次


    public void chargingEnableOnce() {
        if (!ChargingPrefsUtil.getInstance().getSpHelper().contains(app_chargingLocker_enable)) {
            ChargingPrefsUtil.getInstance().getSpHelper().putBoolean(app_chargingLocker_enable, true);
            gaLogger(app_chargingLocker_enable);
        }
    }

    public void chargingScreenShowed() {
        gaLogger(app_chargingLocker_show);
    }

    public void chargingEnableNotificationShowed() {
        gaLogger(notification_chargingLocker_show);
    }

    public void chargingEnableNotificationClicked() {
        gaLogger(notification_chargingLocker_click);
    }

    public void chargingDisableTouchedOnce() {
        if (!ChargingPrefsUtil.getInstance().getSpHelper().contains(app_chargingLocker_disable_clicked)) {
            ChargingPrefsUtil.getInstance().getSpHelper().putBoolean(app_chargingLocker_disable_clicked, true);
            gaLogger(app_chargingLocker_disable_clicked);
        }
    }

    public void chargingDisableConfirmedOnce() {
        if (!ChargingPrefsUtil.getInstance().getSpHelper().contains(app_chargingLocker_disable)) {
            ChargingPrefsUtil.getInstance().getSpHelper().putBoolean(app_chargingLocker_disable, true);
            gaLogger(app_chargingLocker_disable);
        }
    }


    public void nativeAdLoad() {
        gaLogger(NativeAd_Charging_Load);
    }

    public void nativeAdShow() {
        gaLogger(NativeAd_Charging_Show);
    }

    public void nativeAdClick() {
        gaLogger(NativeAd_Charging_Click);
    }


    private void gaLogger(String action) {
        HSAnalytics.logGoogleAnalyticsEvent("charging", "charging", action, "", null, null, null);
    }
}
