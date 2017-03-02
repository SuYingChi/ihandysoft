package com.ihs.chargingscreen.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by zhixiangxiao on 5/6/16.
 */
public class ChargingPrefsUtil {

    private static final String PREFS_FILE_NAME = "ChargingPrefs";

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

    public boolean isChargingEnabled() {
        if (!spHelper.contains(SHOULD_USE_PLIST_SETTING)) {
            if (spHelper.contains(USER_ENABLED_CHARGING)) {
                return spHelper.getBoolean(USER_ENABLED_CHARGING, true);
            } else {
                return HSConfig.optBoolean(true, "Application", "ChargeLocker", "enable");
            }
        }

        if (spHelper.getBoolean(SHOULD_USE_PLIST_SETTING, true)) {
            return HSConfig.optBoolean(true, "Application", "ChargeLocker", "enable");
        } else {
            return isChargingEnableByUser();
        }
    }


    //第一次session退出检查用户是否设置过 charging
    public void setChargingForFirstSession() {
        if (HSSessionMgr.getCurrentSessionId() <= 1) {
            if (!spHelper.contains(USER_ENABLED_CHARGING)) {
                spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, false);
            } else {
                spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, true);
            }
        }
    }


    public boolean isChargingEnableByUser() {
        return spHelper.getBoolean(USER_ENABLED_CHARGING, true);
    }

    public void setChargingEnableByUser(boolean isEnable) {
        spHelper.putBoolean(USER_ENABLED_CHARGING, isEnable);
        spHelper.putBoolean(SHOULD_USE_PLIST_SETTING, false);
    }

    public HSPreferenceHelper getSpHelper() {
        return spHelper;
    }

}
