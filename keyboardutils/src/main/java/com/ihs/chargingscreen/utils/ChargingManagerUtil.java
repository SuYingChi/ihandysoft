package com.ihs.chargingscreen.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.chargingscreen.activity.ChargingScreenAlertActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;
import com.launcher.FloatWindowController;
import com.launcher.LockScreensLifeCycleRegistry;
import com.launcher.chargingscreen.ChargingScreen;

import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.isChargingAlertEnabled;

/**
 * Created by zhixiangxiao on 5/17/16.
 */
public class ChargingManagerUtil {

    private static final int[] BATTERY_LEVELS = {20, 40, 60, 80, 100};

    public static boolean isPushEnabled() {
        //TODO 从 config 读取是否打开 push
//        return HSConfig.optBoolean(true, "libChargingScreen", "ShowPush") && hasPermission("android.permission.SYSTEM_ALERT_WINDOW");
        return true;
    }

    public static boolean hasPermission(String permission) {
        if (permission == null) {
            return false;
        }
        PackageManager pm = HSApplication.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(HSApplication.getContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            //Get Permissions
            String[] requestedPermissions = packageInfo.requestedPermissions;
            if (requestedPermissions == null) {
                return false;
            }

            if (requestedPermissions.length <= 0) {
                return false;
            }
            for (int i = 0; i < requestedPermissions.length; i++) {
                if (permission.equals(requestedPermissions[i])) {
                    return true;
                }
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static String getLeftTimeIndicatorString() {

        String returnString;

        Context context = HSApplication.getContext();

        switch (HSChargingManager.getInstance().getChargingState()) {
            case STATE_DISCHARGING:

                returnString = context.getResources().getString(R.string.charging_module_discharging_left_time_indicator);
                break;
            case STATE_CHARGING_SPEED:

                returnString = context.getResources().getString(R.string.charging_module_speed_charging_left_time_indicator);
                break;
            case STATE_CHARGING_CONTINUOUS:

                returnString = context.getResources().getString(R.string.charging_module_continuous_charging_left_time_indicator);
                break;
            case STATE_CHARGING_TRICKLE:

                returnString = context.getResources().getString(R.string.charging_module_trickle_charging_left_time_indicator);
                break;
            case STATE_CHARGING_FULL:

                returnString = context.getResources().getString(R.string.charging_module_finish_charging_left_time_indicator);
                break;
            default:

                returnString = "";
                break;

        }
        return returnString;
    }

    public static int getImgBatteryVisibleCount() {
        int visibleCount = 1;
        int level = HSChargingManager.getInstance().getBatteryRemainingPercent();

        for (int batteryLevel : BATTERY_LEVELS) {
            if (level > batteryLevel) {
                visibleCount++;
            }
        }

        return visibleCount;
    }

    public static String getChargingLeftTimeString(int chargingLeftMinutes) {
        String leftTime = "";
        if (chargingLeftMinutes / 60 > 0) {
            leftTime += String.valueOf(chargingLeftMinutes / 60) + "H ";
        }
        if (chargingLeftMinutes % 60 > 0) {
            leftTime += String.valueOf(chargingLeftMinutes % 60) + "Min";
        }
        return leftTime;
    }

    public static boolean isChargingEnabled() {
        return ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;
    }

    public static void enableCharging(boolean startChargingActivity) {

        ChargingPrefsUtil.getInstance().setChargingEnableByUser(true);
        HSChargingScreenManager.getInstance().start();

        if (startChargingActivity) {
            startChargingActivity();
        }
    }

    public static void startChargingActivity() {
        if (isChargingAlertEnabled()){
            startChargingAlertActivity("plugeIn");
        } else if (!HSConfig.optBoolean(false, "Application", "Locker", "UseNewLockScreen")) {
            HSLog.d("config use past charging screen");
            try {
                Intent intent = new Intent(HSApplication.getContext(), ChargingScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                ContextCompat.startActivity(HSApplication.getContext(), intent, null);
            } catch (Exception e) {
                HSLog.e(e.getMessage());
            }
        } else {
            HSLog.d("config use new charging screen");
            if (LockerChargingScreenUtils.isCalling()) {
                return;
            }

            // If charging screen activity already exists, do nothing.
            if (LockScreensLifeCycleRegistry.isChargingScreenActive()) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING, HSChargingManager.getInstance().isCharging());
            bundle.putInt(ChargingScreen.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                    HSChargingManager.getInstance().getBatteryRemainingPercent());
            bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_FULL,
                    HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL);
            bundle.putInt(ChargingScreen.EXTRA_INT_CHARGING_LEFT_MINUTES,
                    HSChargingManager.getInstance().getChargingLeftMinutes());
            bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_STATE_CHANGED, false);

            FloatWindowController.getInstance().showChargingScreen(bundle);
        }
    }

    public static void startChargingAlertActivity(String action) {
        try {
            KCAnalytics.logEvent("chargeAlert_show", "Action", action);
            Intent intent = new Intent(HSApplication.getContext(), ChargingScreenAlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            ContextCompat.startActivity(HSApplication.getContext(), intent, null);
        } catch (Exception e) {
            HSLog.e(e.getMessage());
        }
    }

    public static void disableCharging() {
        HSChargingScreenManager.getInstance().stop();
        ChargingPrefsUtil.getInstance().setChargingEnableByUser(false);
    }


}
