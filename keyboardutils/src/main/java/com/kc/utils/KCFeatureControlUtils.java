package com.kc.utils;

import android.content.Context;
import android.text.format.DateUtils;

import com.ihs.commons.utils.HSPreferenceHelper;

public final class KCFeatureControlUtils {
    private static final String PREF_FILE = "kc_utils_fd";
    public static boolean isFeatureReleased(Context context, String featureName, float delayHours) {
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

    private static String getLastActionTimeKey(String featureName) {
        return featureName + "_action_time";
    }

    private static String getActionCountDuringTodayKey(String featureName) {
        return featureName + "_action_count";
    }

    public static int getCountToday(Context context, String featureName) {
        HSPreferenceHelper preferences = HSPreferenceHelper.create(context, PREF_FILE);

        String lastActionTimeKey = getLastActionTimeKey(featureName);
        long lastActionTime = preferences.getLong(lastActionTimeKey, 0);

        if (DateUtils.isToday(lastActionTime)) {
            String countKey = getActionCountDuringTodayKey(featureName);
            return preferences.getInt(countKey, 0);
        } else {
            return 0;
        }
    }

    public static boolean isCountLimitReachedToday(Context context, String featureName, int countLimitPerDay) {
        return getCountToday(context, featureName) >= countLimitPerDay;
    }

    public static void increaseCountToday(Context context, String featureName) {
        HSPreferenceHelper preferences = HSPreferenceHelper.create(context, PREF_FILE);

        int count = getCountToday(context, featureName) + 1;

        preferences.putInt(getActionCountDuringTodayKey(featureName), count);
        preferences.putLong(getLastActionTimeKey(featureName), System.currentTimeMillis());
    }
}
