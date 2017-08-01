package com.artw.lockscreen;

import com.acb.expressads.AcbExpressAdManager;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.PublisherUtils;


public class LockerSettings {

    public static final int LOCKER_MUTED = 0;
    public static final int LOCKER_DEFAULT_DISABLED = 1;
    public static final int LOCKER_DEFAULT_ACTIVE = 2;

    private static final String
            RECORD_CURRENT_PLIST_SETTING = "record_current_plist_setting";

    private static final String PREF_KEY_LOCKER_ENABLED = HSApplication.getContext().getResources().getString(R.string.locker_switcher);
    private static final String PREF_KEY_LOCKER_SHOW_COUNT = "pref_key_locker_show_count";
    private static final String PREF_KEY_LOCKER_ADS_SHOW_COUNT = "pref_key_locker_ads_show_count";
    public static final String LOCKER_PREFS = "lockerprefs";

    private static final String app_screen_locker_enable = "app_screen_locker_enable";
    private static final String app_screen_locker_disable = "app_screen_locker_disable";

    private static final String USER_ENABLED_LOCKER = "user_enabled_locker";

    private static final String LOCKER_ENABLE_SHOW_COUNT = "locker_enable_show_count";
    private static final int LOCKER_ENABLE_MAX_SHOW_COUNT = 5;

    //与键值有关，所以需要使用默认的prefs
    public static boolean isLockerEnabled() {
        return getLockerEnableStates() == LOCKER_DEFAULT_ACTIVE;
    }

    public static void setLockerEnabled(boolean isEnabled) {
        getPref().putBoolean(USER_ENABLED_LOCKER, isEnabled);
        updateLockerSetting();
    }

    public static void increaseLockerShowCount() {
        getPref().putInt(PREF_KEY_LOCKER_SHOW_COUNT, getLockerShowCount() + 1);
    }

    public static int getLockerShowCount() {
        return getPref().getInt(PREF_KEY_LOCKER_SHOW_COUNT, 0);
    }

    public static void setLockerAdsShowCount() {
        getPref().putInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, getLockerShowCount());
    }

    public static int getLockerAdsShowCount() {
        return getPref().getInt(PREF_KEY_LOCKER_ADS_SHOW_COUNT, 0);
    }

    public static HSPreferenceHelper getPref() {
        return HSPreferenceHelper.create(HSApplication.getContext(), LOCKER_PREFS);
    }

    public static HSPreferenceHelper getDefaultPref() {
        return HSPreferenceHelper.getDefault();
    }


    /**
     * locker 是否已经被完全关闭
     *
     * @return
     */
    public static boolean isLockerMuted() {
        return getLockerEnableStates() == LOCKER_MUTED;
    }


    static void initLockerState() {
        if (!getDefaultPref().contains(PREF_KEY_LOCKER_ENABLED)) {
            getDefaultPref().putBoolean(PREF_KEY_LOCKER_ENABLED, isLockerEnabled());
        }
    }

    static void updateLockerSetting() {
        LockerSettings.refreshLockerRecord();

        if (isLockerEnabled()) {
            if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                AcbExpressAdManager.getInstance().activePlacementInProcess(HSApplication.getContext().getString(R.string.ad_placement_locker));
            }
        } else {
            AcbExpressAdManager.getInstance().deactivePlacementInProcess(HSApplication.getContext().getString(R.string.ad_placement_locker));
        }
        getDefaultPref().putBoolean(PREF_KEY_LOCKER_ENABLED, isLockerEnabled());
    }


    public static int getLockerEnableStates() {
        //用户设置过的话，直接返回用户设置的状态。不管plist任何值，包括静默。
        if (getPref().contains(USER_ENABLED_LOCKER)) {
            HSLog.e("locker 获取用户设置");
            return getPref().getBoolean(USER_ENABLED_LOCKER, false) ? LOCKER_DEFAULT_ACTIVE : LOCKER_DEFAULT_DISABLED;
        }

        //老用户会记录 RECORD_CURRENT_PLIST_SETTING 这个值，这里我们可以用来判断是否对他们使用新逻辑
        //老用户使用以前不变的记录，新用户使用（只有当线上开启过一次之后，就不再改变，线上没开起过，将会一直使用新值，直到远端开启过一次
        if (getPref().contains(RECORD_CURRENT_PLIST_SETTING)) {
            HSLog.e("locker 获取已经记录值");
            return getPref().getInt(RECORD_CURRENT_PLIST_SETTING, LOCKER_DEFAULT_DISABLED);
        } else {
            HSLog.e("locker 获取plist");
            //否则 直接取plist
            return getLockerPlistState();
        }
    }

    public static void refreshLockerRecord() {
        //如果没有记录过 并且为开启状态。
        if (!getPref().contains(RECORD_CURRENT_PLIST_SETTING)
                && getLockerPlistState() == LOCKER_DEFAULT_ACTIVE) {
            //记录为已开启。
            getPref().putInt(RECORD_CURRENT_PLIST_SETTING, LOCKER_DEFAULT_ACTIVE);
        }
    }

    static void addLockerEnableShowCount() {
        int count = getLockerEnableShowCount();
        getPref().putInt(LOCKER_ENABLE_SHOW_COUNT, ++count);
    }

    private static int getLockerEnableShowCount() {
        return getPref().getInt(LOCKER_ENABLE_SHOW_COUNT, 0);
    }

    public static boolean isLockerEnableShowSatisfied() {
        return getLockerEnableStates() == LOCKER_DEFAULT_DISABLED
                && !getPref().contains(USER_ENABLED_LOCKER)
                && getLockerEnableShowCount() < HSConfig.optInteger(LOCKER_ENABLE_MAX_SHOW_COUNT, "Application", "Locker", "EnableDialogMaxShowCount");
    }

    public static boolean isLockerEnabledBefore() {
        return getPref().contains(app_screen_locker_enable);
    }

    public static void recordLockerEnableOnce() {
        if (!getPref().contains(app_screen_locker_enable)) {
            getPref().putBoolean(app_screen_locker_enable, true);
            HSAnalytics.logEvent(app_screen_locker_enable, app_screen_locker_enable, "lockerEnabled", "install_type", PublisherUtils.getInstallType());
        }
    }

    public static void recordLockerDisableOnce() {
        if (!getPref().contains(app_screen_locker_disable)) {
            getPref().putBoolean(app_screen_locker_disable, true);
            HSAnalytics.logEvent(app_screen_locker_disable, app_screen_locker_disable, "lockerDisabled", "install_type", PublisherUtils.getInstallType());
        }
    }

    private static int getLockerPlistState() {
        return HSConfig.optInteger(LOCKER_MUTED, "Application", "Locker", "state");
    }

    public static boolean isUserTouchedLockerSettings() {
        return getPref().contains(USER_ENABLED_LOCKER);
    }
}
