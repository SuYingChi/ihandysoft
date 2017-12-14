package com.ihs.keyboardutils.appsuggestion;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.kc.utils.KCFeatureControlUtils;

import static com.ihs.keyboardutils.appsuggestion.AppSuggestionManager.FEATURE_NAME;

/**
 * Created by Arthur on 17/12/11.
 */

public class AppSuggestionSetting {

    private static final String PREFS_FILE_NAME = "pref_appsuggestion";
    private static final String USER_ENABLED_APPSUGGESTION = "user_enabled_appsuggestion";

    private static final int APPSUGGESTION_MUTED = 0;
    private static final int APPSUGGESTION_DEFAULT_ACTIVE = 1;
    private static final int APPSUGGESTION_DEFAULT_DISABLED = 2;
    private static final String RECORD_CURRENT_PLIST_SETTING = "record_current_plist_setting";
    private static final String SP_LAST_SHOW_TIME = "sp_last_show_time";
    private static final String DEFAULT_PREF_KEY_APP_SUGGESTION_ENABLED = "pref_key_app_suggestion_enabled";
    private long showInterval = 0;

    private static final String USER_ENABLED_SUGGESTION = "user_enabled_suggestion";


    private static AppSuggestionSetting instance;
    private final HSPreferenceHelper spHelper;
    private static final String RECENT_APP_LIST = "recent_app_list";


    public static AppSuggestionSetting getInstance() {
        if (instance == null) {
            instance = new AppSuggestionSetting();
        }
        return instance;
    }

    private AppSuggestionSetting() {
        spHelper = HSPreferenceHelper.create(HSApplication.getContext(), PREFS_FILE_NAME);
    }

    public int getAppSuggestionEnableStates() {
        //用户设置过的话，直接返回用户设置的状态。不管plist任何值，包括静默。
        if (spHelper.contains(USER_ENABLED_APPSUGGESTION)) {
            HSLog.e("appsuggestion 获取用户设置");
            return spHelper.getBoolean(USER_ENABLED_APPSUGGESTION, false) ? APPSUGGESTION_DEFAULT_ACTIVE : APPSUGGESTION_DEFAULT_DISABLED;
        }

        //老用户会记录 RECORD_CURRENT_PLIST_SETTING 这个值，这里我们可以用来判断是否对他们使用新逻辑
        //老用户使用以前不变的记录，新用户使用（只有当线上开启过一次之后，就不再改变，线上没开起过，将会一直使用新值，直到远端开启过一次
        if (spHelper.contains(RECORD_CURRENT_PLIST_SETTING)) {
            HSLog.e("APPSUGGESTION 获取已经记录值");
            return spHelper.getInt(RECORD_CURRENT_PLIST_SETTING, APPSUGGESTION_DEFAULT_DISABLED);
        } else {
            HSLog.e("APPSUGGESTION 获取plist");
            //否则 直接取plist
            return getPlistState();
        }
    }

    static void initEnableState() {
        if (!HSPreferenceHelper.getDefault().contains(DEFAULT_PREF_KEY_APP_SUGGESTION_ENABLED)) {
            HSPreferenceHelper.getDefault().putBoolean(DEFAULT_PREF_KEY_APP_SUGGESTION_ENABLED, getInstance().isEnabled());
        }
    }

    public boolean isMuted() {
        return AppSuggestionSetting.getInstance().getAppSuggestionEnableStates() == APPSUGGESTION_MUTED;
    }

    public int getPlistState() {
        return HSConfig.optInteger(APPSUGGESTION_DEFAULT_ACTIVE, "Application", "AppSuggestion", "state");
    }

    public boolean canShowAppSuggestion() {
        long lastShowTime = spHelper.getLong(SP_LAST_SHOW_TIME, 0);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShowTime >= showInterval) {
            spHelper.putLong(SP_LAST_SHOW_TIME, currentTime);
            return true;
        }
        return false;
    }

    public boolean isEnabled() {
        return getAppSuggestionEnableStates() == APPSUGGESTION_DEFAULT_ACTIVE;
    }

    public void setEnabled(boolean isEnabled) {
        spHelper.putBoolean(USER_ENABLED_SUGGESTION, isEnabled);
        updateAppSuggestionSetting();
    }

    public void refreshAppSuggestionRecord() {
        //如果没有记录过 并且为开启状态。
        if (!spHelper.contains(RECORD_CURRENT_PLIST_SETTING)
                && getPlistState() == APPSUGGESTION_DEFAULT_ACTIVE) {
            //记录为已开启。
            spHelper.putInt(RECORD_CURRENT_PLIST_SETTING, APPSUGGESTION_DEFAULT_ACTIVE);
        }
    }

    public void updateAppSuggestionSetting() {
        refreshAppSuggestionRecord();
        HSPreferenceHelper.getDefault().putBoolean(DEFAULT_PREF_KEY_APP_SUGGESTION_ENABLED, isEnabled());
    }


    public void setShowInterval(long showInterval) {
        this.showInterval = showInterval;
    }

    public void saveRecentList(String recentApps) {
        spHelper.putString(RECENT_APP_LIST, recentApps);
    }

    public String getSavedRecentList() {
        return spHelper.getString(RECENT_APP_LIST, "");
    }

    public boolean isFeatureEnabled() {
        return KCFeatureControlUtils.isFeatureReleased(HSApplication.getContext(),
                FEATURE_NAME,
                HSConfig.optInteger(0, "Application", FEATURE_NAME, "HoursFromFirstUse"));
    }
}
