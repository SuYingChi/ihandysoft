package com.ihs.chargingscreen.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

import static com.ihs.chargingscreen.utils.ChargingAnalytics.app_chargingLocker_enable;

/**
 * Created by zhixiangxiao on 5/6/16.
 */
public class ChargingPrefsUtil {
    public static final String PLUG_MAX_TIME = "plug_max_time";
    public static final String UNPLUG_MAX_TIME = "unplug_max_time";
    public static final String FULL_CHARGED_MAX_TIME = "full_charged_max_time";
    private static final int CHARGING_STATE_MAX_APPEAR_TIMES = 3;

    public static final int CHARGING_MUTED = 0;
    public static final int CHARGING_DEFAULT_DISABLED = 1;
    public static final int CHARGING_DEFAULT_ACTIVE = 2;


    private static final String PREFS_FILE_NAME = "chargingsp";

    private static final String PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT = "PREFS_FULL_CHARGED_PUSH_SHOWED_COUNT";
    private static HSPreferenceHelper spHelper;
    private static ChargingPrefsUtil instance;

    public static final String USER_ENABLED_CHARGING = "user_enabled_charging";
    private static final String
            SHOULD_USE_PLIST_SETTING = "should_use_plist_setting";


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

    /**
     * charging 是否已经被完全关闭
     *
     * @return
     */
    public static boolean isChargingMuted() {
        return HSConfig.optInteger(CHARGING_MUTED, "Application", "ChargeLocker", "state") == CHARGING_MUTED;
    }

    public int getChargingEnableStates() {
        int moduleStates = HSConfig.optInteger(CHARGING_MUTED, "Application", "ChargeLocker", "state");
        switch (moduleStates) {
            case CHARGING_MUTED:
                return CHARGING_MUTED;
        }
        if (!spHelper.contains(SHOULD_USE_PLIST_SETTING)) {
            if (spHelper.contains(USER_ENABLED_CHARGING)) {
                return spHelper.getBoolean(USER_ENABLED_CHARGING, true) ? CHARGING_DEFAULT_ACTIVE : CHARGING_DEFAULT_DISABLED;
            } else {
                return moduleStates;
            }
        }

        if (spHelper.getBoolean(SHOULD_USE_PLIST_SETTING, true)) {
            return moduleStates;
        } else {
            return isChargingEnableByUser() ? CHARGING_DEFAULT_ACTIVE : CHARGING_DEFAULT_DISABLED;
        }
    }


    //第一次session退出检查用户是否设置过 charging
    public void setChargingForFirstSession() {
        if (HSSessionMgr.getCurrentSessionId() <= 1) {
            if (spHelper.contains(USER_ENABLED_CHARGING)) {
                spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, false);
            } else {
                spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, true);
            }
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

    public void setChargingEnableByUser(boolean isEnable) {
        spHelper.putBoolean(USER_ENABLED_CHARGING, isEnable);
        spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, false);
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
}
