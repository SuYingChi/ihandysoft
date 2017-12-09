package com.kc.utils;

import android.content.Context;

import com.ihs.commons.utils.HSPreferenceHelper;

public final class FeatureDelayReleaseUtils {
    private static final String PREF_FILE = "kc_utils_fd";
    public static boolean isFeatureAvailable(Context context, String featureName, float delayHours) {
        HSPreferenceHelper preferences = HSPreferenceHelper.create(context, PREF_FILE);
        long currentTime = System.currentTimeMillis();
        long firstCheckTime;
        if (!preferences.contains(featureName)) {
            firstCheckTime = currentTime;
            preferences.putLong(featureName, firstCheckTime);
        } else {
            firstCheckTime = preferences.getLong(featureName, 0L);
        }
        return currentTime >= firstCheckTime + (long)(delayHours * 3600 * 1000);
    }
}
