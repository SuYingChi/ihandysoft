package com.ihs.feature.junkclean.util;

import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.PreferenceHelper;

public class JunkPreferenceHelper {

    public static void putBoolean(String key, boolean value) {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putBoolean(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getBoolean(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putLong(key, value);
    }

    public static long getLong(String key, long defaultValue) {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getLong(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putInt(key, value);
    }

    public static int getInt(String key, int defaultValue) {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getInt(key, defaultValue);
    }

}
