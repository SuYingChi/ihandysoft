package com.ihs.chargingscreen.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by yanxia on 2017/4/21.
 */

public final class FeatureDelayReleaseUtil {
    private static final String PREF_FILE_NAME = "FeatureDelayReleaseUtilSp";

    public static boolean checkFeatureReadyToWork(String featureName, float delayHours) {
        HSPreferenceHelper preferences = HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_NAME);
        long currentTime = System.currentTimeMillis();
        long appFirstLaunchTime;
        if (!preferences.contains(featureName)) {
            appFirstLaunchTime = currentTime;
            preferences.putLong(featureName, appFirstLaunchTime);
        } else {
            appFirstLaunchTime = preferences.getLong(featureName, 0L);
        }
        return currentTime >= appFirstLaunchTime + (long)(delayHours * 3600 * 1000);
    }
}
