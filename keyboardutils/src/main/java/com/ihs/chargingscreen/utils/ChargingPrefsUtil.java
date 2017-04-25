package com.ihs.chargingscreen.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
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

    //这个值是为了让老用户使用现在的
    private static final String
            RECORD_CURRENT_PLIST_SETTING = "record_current_plist_setting";


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
        return getChargingEnableStates() == CHARGING_MUTED;
    }


    public static int getChargingEnableStates() {
        //用户设置过的话，直接返回用户设置的状态。不管plist任何值，包括静默。
        if (spHelper.contains(USER_ENABLED_CHARGING)) {
            HSLog.e("CHARGING 获取用户设置" );
            return spHelper.getBoolean(USER_ENABLED_CHARGING, false) ? CHARGING_DEFAULT_ACTIVE : CHARGING_DEFAULT_DISABLED;
        }

        //用户没有设置过，并且不在第一个session的话。就返回第一个session结束时，被记录的plist值。
        if (HSSessionMgr.getCurrentSessionId() > 0) {
            HSLog.e("CHARGING 获取已经记录值" );
            return spHelper.getInt(RECORD_CURRENT_PLIST_SETTING, CHARGING_DEFAULT_DISABLED);
        } else {
            //第一个session就取plist值
            HSLog.e("CHARGING 获取plist" );
            return getChargingPlistConfig();
        }
    }


    //前3次session退出检查用户是否设置过 charging
    public void setChargingForFirstSession() {
        if (HSSessionMgr.getCurrentSessionId() < 3) {
            //如果用户设置过了就不用记录
            if (!spHelper.contains(USER_ENABLED_CHARGING) && !spHelper.contains(RECORD_CURRENT_PLIST_SETTING)) {
                HSLog.e("chagring 正在记录");
                //没有设置的话就获取当前plist配置 并记录下来。
                spHelper.putInt(RECORD_CURRENT_PLIST_SETTING, getChargingPlistConfig());
            }
        }
        HSLog.e("chagring 已经记录 ");
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
        return HSConfig.optInteger(CHARGING_MUTED, "Application", "ChargeLocker", "state");
    }
}
