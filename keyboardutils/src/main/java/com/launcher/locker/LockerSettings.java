package com.launcher.locker;

import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;


public class LockerSettings {
    //Multi-process
    public static final String PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN = "pref_key_locker_toggle_guide_shown";

    public static boolean isLockerToggleGuideShown() {
        return PreferenceHelper.get(LauncherFiles.LOCKER_PREFS).getBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, false);
    }

    public static void setLockerToggleGuideShown() {
        PreferenceHelper.get(LauncherFiles.LOCKER_PREFS).putBoolean(PREF_KEY_LOCKER_TOGGLE_GUIDE_SHOWN, true);
    }
}
