package com.ihs.feature.battery;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.SystemSettingsManager;

import java.util.HashMap;

public class BatteryModeManager implements SystemSettingsManager.ISystemSettingsListener {

    //region Static Mode Configs
    public static final HashMap<SystemSettingsManager.SettingsItem, Integer> MODE_MAX_SAVER = new HashMap<>();
    public static final HashMap<SystemSettingsManager.SettingsItem, Integer> MODE_SMART_SAVER = new HashMap<>();
    public static final SystemSettingsManager.SettingsItem[] batteryItems = {
            SystemSettingsManager.SettingsItem.BRIGHTNESS,
            SystemSettingsManager.SettingsItem.SCREEN_TIMEOUT,
            SystemSettingsManager.SettingsItem.VIBRATE,
            SystemSettingsManager.SettingsItem.WIFI,
            SystemSettingsManager.SettingsItem.BLUETOOTH,
            SystemSettingsManager.SettingsItem.MOBILE_DATA,
            SystemSettingsManager.SettingsItem.AUTO_SYNC,
            SystemSettingsManager.SettingsItem.HAPTIC_FEEDBACK
    };

    static {
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.BRIGHTNESS, 26);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.SCREEN_TIMEOUT, 15);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.VIBRATE, 0);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.WIFI, 0);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.BLUETOOTH, 0);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.MOBILE_DATA, 0);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.AUTO_SYNC, 0);
        MODE_MAX_SAVER.put(SystemSettingsManager.SettingsItem.HAPTIC_FEEDBACK, 0);

        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.BRIGHTNESS, -1);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.SCREEN_TIMEOUT, 30);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.VIBRATE, 0);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.WIFI, 1);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.BLUETOOTH, 0);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.MOBILE_DATA, 1);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.AUTO_SYNC, 0);
        MODE_SMART_SAVER.put(SystemSettingsManager.SettingsItem.HAPTIC_FEEDBACK, 0);
    }
    //endregion

    private Context mContext;
    private SystemSettingsManager mSystemSettingsManager;

    public BatteryModeManager(Context context) {
        mContext = context;
        mSystemSettingsManager = new SystemSettingsManager(mContext);
        mSystemSettingsManager.register(this);
    }

    public HashMap<SystemSettingsManager.SettingsItem, Integer> getCurrentMode() {
        HashMap<SystemSettingsManager.SettingsItem, Integer> currentMode = new HashMap<>();
        for (SystemSettingsManager.SettingsItem item : batteryItems) {
            currentMode.put(item, mSystemSettingsManager.getSystemSettingsItemState(item));
        }
        return currentMode;
    }

    public void switchToMode(HashMap<SystemSettingsManager.SettingsItem, Integer> mode) {
        for (SystemSettingsManager.SettingsItem item : batteryItems) {
            mSystemSettingsManager.setSystemSettingsItemState(item, mode.get(item));
        }
    }

    @Override
    public void onSystemSettingsStateChanged(SystemSettingsManager.SettingsItem item, int state) {

    }

    public void release() {
        mSystemSettingsManager.unRegister();
    }

    public static boolean isModeEqual(HashMap<SystemSettingsManager.SettingsItem, Integer> modeSrc,
                                      HashMap<SystemSettingsManager.SettingsItem, Integer> modeDst) {
        boolean equal = true;
        for (SystemSettingsManager.SettingsItem item : batteryItems) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && item.equals(SystemSettingsManager.SettingsItem.MOBILE_DATA)) {
                continue;
            }
            if (!modeSrc.get(item).equals(modeDst.get(item))) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    //region HSPreference Settings
    private static final String PREF_KEY_LAUNCHED = "battery_mode_activity_launched";
    public static final String PREF_KEY_LAUNCHED_LEGACY = "pref_key_is_first_launch"; // Deprecated since v1.4.2
    public static final String PREF_KEY_LAST_MODE = "pref_key_last_mode";
    public static final String PREF_KEY_PREVIOUS_DETAIL = "pref_key_previous_detail";

    static boolean isFirstLaunch() {
        return !PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getBoolean(PREF_KEY_LAUNCHED, false);
    }

    public static void setFirstLaunched() {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putBoolean(PREF_KEY_LAUNCHED, true);
    }

    static int getLastMode() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_LAST_MODE,
                BatteryModeActivity.ModeType.CURRENT_SAVER.ordinal());
    }

    static void setLastMode(BatteryModeActivity.ModeType modeType) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putInt(PREF_KEY_LAST_MODE, modeType.ordinal());
    }

    static HashMap<SystemSettingsManager.SettingsItem, Integer> getPreviousMode() {
        String detailStr = getPreviousDetail();
        if (TextUtils.isEmpty(detailStr)) {
            return null;
        }

        String[] details = detailStr.split(";");
        HashMap<SystemSettingsManager.SettingsItem, Integer> previousMode = new HashMap<>();
        for (int i = 0; i < batteryItems.length; i++) {
            previousMode.put(batteryItems[i], Integer.parseInt(details[i]));
        }
        return previousMode;
    }

    private static String getPreviousDetail() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getString(PREF_KEY_PREVIOUS_DETAIL, "");
    }

    static void setPreviousDetail(HashMap<SystemSettingsManager.SettingsItem, Integer> mode) {
        StringBuilder builder = new StringBuilder();
        for (SystemSettingsManager.SettingsItem item : batteryItems) {
            builder.append(mode.get(item)).append(";");
        }
        setPreviousDetail(builder.toString());
    }

    private static void setPreviousDetail(String detail) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putString(PREF_KEY_PREVIOUS_DETAIL, detail);
    }
    //endregion
}
