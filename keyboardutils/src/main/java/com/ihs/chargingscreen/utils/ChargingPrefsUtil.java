package com.ihs.chargingscreen.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.keyboardutils.iap.RemoveAdsManager;

import net.appcloudbox.ads.expressad.AcbExpressAdManager;

import static com.ihs.chargingscreen.utils.ChargingAnalytics.app_chargingLocker_enable;

/**
 * Created by zhixiangxiao on 5/6/16.
 */
public class ChargingPrefsUtil {
    public static final String PLUG_MAX_TIME = "plug_max_time";
    public static final String UNPLUG_MAX_TIME = "unplug_max_time";
    public static final String FULL_CHARGED_MAX_TIME = "full_charged_max_time";
    private static final int CHARGING_STATE_MAX_APPEAR_TIMES = 3;
    public static final String PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN = "pref_key_charging_screen_battery_menu_tip_shown";

    public static final int CHARGING_MUTED = 0;
    public static final int CHARGING_DEFAULT_DISABLED = 1;
    public static final int CHARGING_DEFAULT_ACTIVE = 2;


    private static final String PREFS_FILE_NAME = "chargingsp";

    private static final String PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT = "PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT";
    private static HSPreferenceHelper spHelper;
    private static ChargingPrefsUtil instance;

    public static final String USER_ENABLED_CHARGING;
    private static final String RECORD_CURRENT_PLIST_SETTING;    //这个值是为了让老用户使用现在的

    static {
        if (isChargingAlertEnabled()) {
            USER_ENABLED_CHARGING = "user_enabled_charging_alert";
            RECORD_CURRENT_PLIST_SETTING = "charging_alert_record_current_plist_setting";
        } else {
            USER_ENABLED_CHARGING = "user_enabled_charging";
            RECORD_CURRENT_PLIST_SETTING = "record_current_plist_setting";
        }
    }

    public static ChargingPrefsUtil getInstance() {
        if (instance == null) {
            instance = new ChargingPrefsUtil();
        }
        return instance;
    }

    private ChargingPrefsUtil() {
        spHelper = HSPreferenceHelper.create(HSApplication.getContext(), PREFS_FILE_NAME);
    }

    public static int getFullChargedPushShowedCount() {
        return HSPreferenceHelper.create(HSApplication.getContext(), PREFS_FILE_NAME).getInt(PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT, 0);
    }

    public static void increaseFullChargedPushShowedCount() {
        final int fullChargedPushShowedCount = getFullChargedPushShowedCount();
        HSPreferenceHelper.create(HSApplication.getContext(), PREFS_FILE_NAME).putInt(PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT, fullChargedPushShowedCount + 1);
    }

    public static boolean isChargingAlertEnabled() {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= HSConfig.optInteger(Integer.MAX_VALUE, "Application", "ChargeAlert", "StartVersion") &&
                HSConfig.optBoolean(false, "Application", "ChargeAlert", "ChargingAlertEnabled");
    }

    /**
     * charging 是否已经被完全关闭
     *
     * @return
     */
    public static boolean isChargingMuted() {
        return getChargingEnableStates() == CHARGING_MUTED;
    }

    public static int getChargingEnableStates() {
        if (spHelper == null) {
            getInstance();
        }

        //charging老界面的用户如果需要隐藏功能，则会隐藏charging功能
        if (!isChargingAlertEnabled() && LockerChargingSpecialConfig.getInstance().isHideLockerAndCharging()) {
            return CHARGING_MUTED;
        }

        //用户设置过的话，直接返回用户设置的状态。不管plist任何值，包括静默。
        if (spHelper.contains(USER_ENABLED_CHARGING)) {
            HSLog.e("CHARGING 获取用户设置");
            return spHelper.getBoolean(USER_ENABLED_CHARGING, false) ? CHARGING_DEFAULT_ACTIVE : CHARGING_DEFAULT_DISABLED;
        }


        //老用户会记录 RECORD_CURRENT_PLIST_SETTING 这个值，这里我们可以用来判断是否对他们使用新逻辑
        //老用户使用以前不变的记录，新用户使用（只有当线上开启过一次之后，就不再改变，线上没开起过，将会一直使用新值，直到远端开启过一次
        if (spHelper.contains(RECORD_CURRENT_PLIST_SETTING)) {
            HSLog.e("CHARGING 老用户读值");
            return spHelper.getInt(RECORD_CURRENT_PLIST_SETTING, CHARGING_DEFAULT_DISABLED);
        } else {
            //否则 直接取plist
            HSLog.e("CHARGING 获取plist");
            return getChargingPlistConfig();
        }
    }

    public static void refreshChargingRecord() {
        //如果没有记录过 并且为开启状态。
        if (!spHelper.contains(RECORD_CURRENT_PLIST_SETTING)
                && getChargingPlistConfig() == CHARGING_DEFAULT_ACTIVE) {
            //记录为已开启。
            spHelper.putInt(RECORD_CURRENT_PLIST_SETTING, CHARGING_DEFAULT_ACTIVE);
        }
    }


    private int chargingNotifyAppearTimes(String chargingType) {
        return spHelper.getInt(chargingType, 0);
    }

    private void incrementNotifyAppearTimes(String chargingType) {
        spHelper.putInt(chargingType, chargingNotifyAppearTimes(chargingType) + 1);
    }

    public boolean isChargingEnableByUser() {
        return spHelper.getBoolean(USER_ENABLED_CHARGING, false);
    }

    //用户每次更改设置都要记录值，以便下次直接读取。
    public void setChargingEnableByUser(boolean isEnable) {
        if (isEnable) {
            if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                if (ChargingPrefsUtil.isChargingAlertEnabled()) {
                    AcbExpressAdManager.getInstance().activePlacementInProcess(HSChargingScreenManager.getInstance().getChargingAlertAdsPlacementName());
                } else {
                    if (LockerChargingSpecialConfig.getInstance().shouldShowAd()) {
                        AcbExpressAdManager.getInstance().activePlacementInProcess(HSChargingScreenManager.getInstance().getChargingActivityAdsPlacementName());
                    }
                }
            }
        } 
        spHelper.putBoolean(USER_ENABLED_CHARGING, isEnable);
    }

    public HSPreferenceHelper getSpHelper() {
        return spHelper;
    }


    public boolean isChagringNotifyMaxAppearTimesAcheived(String chargingType) {
        if (chargingNotifyAppearTimes(chargingType) < CHARGING_STATE_MAX_APPEAR_TIMES) {
            incrementNotifyAppearTimes(chargingType);
            return true;
        } else {
            return false;
        }
    }

    public boolean isChargingEnabledBefore() {
        return spHelper.contains(app_chargingLocker_enable);
    }


    private static int getChargingPlistConfig() {
        if (isChargingAlertEnabled()) {
            return HSConfig.optInteger(CHARGING_MUTED, "Application", "ChargeAlert", "State");
        }
        return HSConfig.optInteger(CHARGING_MUTED, "Application", "ChargeLocker", "state");
    }

    public static boolean isBatteryTipShown() {
        return PreferenceHelper.getDefault().getBoolean(PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN, false);
    }

    public static void setBatteryTipShown() {
        PreferenceHelper.getDefault().putBoolean(PREF_KEY_CHARGING_SCREEN_BATTERY_MENU_TIP_SHOWN, true);
    }

    public static boolean isUserTouchedChargingSetting() {
        return spHelper.contains(USER_ENABLED_CHARGING);
    }
}
